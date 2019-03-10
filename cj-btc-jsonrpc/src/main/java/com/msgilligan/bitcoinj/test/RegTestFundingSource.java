package com.msgilligan.bitcoinj.test;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import org.consensusj.jsonrpc.JsonRpcException;
import com.msgilligan.bitcoinj.json.pojo.Outpoint;
import com.msgilligan.bitcoinj.json.pojo.SignedRawTransaction;
import com.msgilligan.bitcoinj.json.pojo.TxOutInfo;
import com.msgilligan.bitcoinj.json.pojo.UnspentOutput;
import com.msgilligan.bitcoinj.json.conversion.BitcoinMath;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FundingSource using RegTest mining
 */
public class RegTestFundingSource implements FundingSource {
    final Integer defaultMaxConf = 9999999;
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);
    protected BitcoinExtendedClient client;

    public RegTestFundingSource(BitcoinExtendedClient client) {
        this.client = client;
    }

    /**
     * Generate blocks and fund an address with requested amount of BTC
     *
     * TODO: Improve performance. Can we mine multiple blocks with a single RPC?
     *
     * @param toAddress Address to fund with BTC
     * @param requestedBtc Amount of BTC to "mine" and send
     * @return The hash of transaction that provided the funds.
     */
    @Override
    public Sha256Hash requestBitcoin(Address toAddress, Coin requestedBtc) throws JsonRpcException, IOException {
        log.debug("requestBitcoin requesting {}", requestedBtc);
        if (requestedBtc.value > NetworkParameters.MAX_MONEY.value) {
            throw new IllegalArgumentException("request exceeds MAX_MONEY");
        }
        long amountGatheredSoFar = 0;
        ArrayList<Outpoint> inputs = new ArrayList<Outpoint>();

        // Newly mined coins need to mature to be spendable
        final int minCoinAge = 100;

        if (client.getBlockCount() < minCoinAge) {
            client.generate(minCoinAge - client.getBlockCount());
        }

        while (amountGatheredSoFar < requestedBtc.value) {
            client.generate();
            int blockIndex = client.getBlockCount() - minCoinAge;
            Block block = client.getBlock(blockIndex);
            List<Transaction> blockTxs = block.getTransactions();
            Sha256Hash coinbaseTx = blockTxs.get(0).getTxId();
            TxOutInfo txout = client.getTxOut(coinbaseTx, 0);

            // txout is empty, if output was already spent
            if (txout != null && txout.getValue().value > 0) {
                log.debug("txout = {}, value = {}", txout, txout.getValue().value);

                amountGatheredSoFar += txout.getValue().value;
                inputs.add(new Outpoint(coinbaseTx, 0));
            }
            log.debug("amountGatheredSoFar = {}", BitcoinMath.satoshiToBtc(amountGatheredSoFar));
        }

        // Don't care about change, we mine it anyway
        String unsignedTxHex = client.createRawTransaction(inputs, Collections.singletonMap(toAddress, requestedBtc));
        SignedRawTransaction signingResult = client.signRawTransaction(unsignedTxHex);

        assert signingResult.isComplete();

        String signedTxHex = signingResult.getHex();
        Sha256Hash txid = client.sendRawTransaction(signedTxHex, true);

        return txid;
    }

    /**
     * Create an address and fund it with bitcoin
     *
     * @param amount
     * @return Newly created address with the requested amount of bitcoin
     */
    public Address createFundedAddress(Coin amount) throws Exception {
        Address address = client.getNewAddress();
        requestBitcoin(address, amount);
        return address;
    }

    /**
     * Create everything needed to assemble a custom transaction
     * @param amount Amount of BTC to be available on new address
     * @return An address, private key, and list of unspent outputs
     * @throws JsonRpcException
     * @throws IOException
     */
    public TransactionIngredients createIngredients(Coin amount) throws JsonRpcException, IOException {
        TransactionIngredients ingredients = new TransactionIngredients();
        Address address = client.getNewAddress();
        requestBitcoin(address, amount);
        ingredients.address = address;
        ingredients.privateKey = client.dumpPrivKey(address);
        ingredients.outPoints = client.listUnspentOutPoints(address);
        return ingredients;
    }

    @Override
    public void fundingSourceMaintenance() {
        try {
            consolidateCoins();
        } catch (JsonRpcException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Collects *all* unspent outputs and spends the whole amount minus `stdRelayTxFee`, which is sent
     * to a new address, as fee, to sweep dust and to minimize the number of unspent outputs, to avoid creating too
     * large transactions. No new block is generated afterwards.
     *
     * Can be used in cleanupSpec() methods of integration tests.
     *
     * @see <a href="https://github.com/OmniLayer/OmniJ/issues/50">Issue #50 on GitHub</a>
     *
     * @return True, if enough outputs with a value of at least {@code stdRelayTxFee} were spent
     */
     void consolidateCoins() throws JsonRpcException, IOException {
        long amountIn = 0;
        List<Outpoint> inputs = new ArrayList<Outpoint>();
        List<UnspentOutput> unspentOutputs = client.listUnspent(1,defaultMaxConf);

        // Gather inputs
        for (UnspentOutput unspentOutput : unspentOutputs) {
            amountIn += unspentOutput.getAmount().value;
            inputs.add(new Outpoint(unspentOutput.getTxid(), unspentOutput.getVout()));

        }

        // Check if there is a sufficient high amount to sweep at all
        if (amountIn < client.stdRelayTxFee.value) {
            return; //false;
        }

        // No receiver, just spend most of it as fee (!)
        Map<Address,Coin> outputs = new HashMap<Address, Coin>();
        outputs.put(client.getNewAddress(), client.stdRelayTxFee);

        String unsignedTxHex = client.createRawTransaction(inputs, outputs);
        SignedRawTransaction signingResult = client.signRawTransaction(unsignedTxHex);

        Boolean complete = signingResult.isComplete();
        assert complete;

        String signedTxHex = signingResult.getHex();
        Object txid = client.sendRawTransaction(signedTxHex, true);

        return; //true;
    }


}
