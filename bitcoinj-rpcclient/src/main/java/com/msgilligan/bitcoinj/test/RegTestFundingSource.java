package com.msgilligan.bitcoinj.test;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import com.msgilligan.bitcoinj.rpc.JsonRPCException;
import com.msgilligan.bitcoinj.rpc.Outpoint;
import com.msgilligan.bitcoinj.rpc.UnspentOutput;
import com.msgilligan.bitcoinj.rpc.conversion.BitcoinMath;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FundingSource using RegTest mining
 */
public class RegTestFundingSource implements FundingSource {
    final Integer defaultMaxConf = 9999999;
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);
    private BitcoinExtendedClient client;

    public RegTestFundingSource(BitcoinExtendedClient client) {
        this.client = client;
    }

    /**
     * Generate blocks and fund an address with requested amount of BTC
     *
     * TODO: Improve performance. Can we mine multiple blocks with a single RPC?
     *
     * @param toAddress Address to fund with BTC
     * @param requestedBTC Amount of BTC to "mine" and send
     * @return
     */
    @Override
    public Sha256Hash requestBitcoin(Address toAddress, Coin requestedAmount) throws JsonRPCException, IOException {
        // Refactor code from BTCTestSupport to here.
        log.debug("requestBitcoin requesting {}", requestedAmount);
        if (requestedAmount.value > NetworkParameters.MAX_MONEY.value) {
            throw new IllegalArgumentException("request exceeds MAX_MONEY");
        }
        long amountGatheredSoFar = 0;
        ArrayList<Outpoint> inputs = new ArrayList<Outpoint>();

        // Newly mined coins need to mature to be spendable
        final int minCoinAge = 100;

        if (client.getBlockCount() < minCoinAge) {
            client.generateBlocks((long)minCoinAge - client.getBlockCount());
        }

        while (amountGatheredSoFar < requestedAmount.value) {
            client.generateBlock();
            int blockIndex = client.getBlockCount() - minCoinAge;
            Map<String, Object> block = client.getBlock(blockIndex);
            List<String> blockTxs = (List<String>) block.get("tx");
            Sha256Hash coinbaseTx = Sha256Hash.wrap(blockTxs.get(0));
            Map<String, Object>  txout = client.getTxOut(coinbaseTx, 0);

            // txout is empty, if output was already spent
            if (txout != null && txout.containsKey("value")) {
                log.debug("txout = {}", txout);
                BigDecimal amountBTCbd = BigDecimal.valueOf((Double) txout.get("value"));

                long amountSatoshi = BitcoinMath.btcToSatoshi(amountBTCbd);
                amountGatheredSoFar += amountSatoshi;
                inputs.add(new Outpoint(coinbaseTx, 0));
            }
            log.debug("amountGatheredSoFar = {}", BitcoinMath.satoshiToBtc(amountGatheredSoFar));
        }

        // Don't care about change, we mine it anyway
        HashMap<Address, Coin> outputs = new HashMap<Address, Coin>();
        outputs.put(toAddress, requestedAmount);

        String unsignedTxHex = client.createRawTransaction(inputs, outputs);
        Map<String, Object> signingResult = client.signRawTransaction(unsignedTxHex);

        assert ((Boolean) signingResult.get("complete"));

        String signedTxHex = (String) signingResult.get("hex");
        Sha256Hash txid = client.sendRawTransaction(signedTxHex, true);

        return txid;
    }

    public Address createFundedAddress(Coin amount) throws Exception {
        Address address = client.getNewAddress();
        requestBitcoin(address, amount);
        return address;
    }

    @Override
    public void fundingSourceMaintenance() {
        try {
            consolidateCoins();
        } catch (JsonRPCException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Collects <b>all</b> unspent outputs and spends the whole amount minus {@code stdRelayTxFee}, which is sent
     * to a new address, as fee, to sweep dust and to minimize the number of unspent outputs, to avoid creating too
     * large transactions. No new block is generated afterwards.
     *
     * Can be used in cleanupSpec() methods of integration tests.
     *
     * @see <a href="https://github.com/OmniLayer/OmniJ/issues/50">Issue #50 on GitHub</a>
     *
     * @return True, if enough outputs with a value of at least {@code stdRelayTxFee} were spent
     */
    Boolean consolidateCoins() throws JsonRPCException, IOException {
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
            return false;
        }

        // No receiver, just spend most of it as fee (!)
        Map<Address,Coin> outputs = new HashMap<Address, Coin>();
        outputs.put(client.getNewAddress(), client.stdRelayTxFee);

        String unsignedTxHex = client.createRawTransaction(inputs, outputs);
        Map<String, Object> signingResult = client.signRawTransaction(unsignedTxHex);

        Boolean complete = (Boolean) signingResult.get("complete");
        assert complete;

        String signedTxHex = (String) signingResult.get("hex");
        Object txid = client.sendRawTransaction(signedTxHex, true);

        return true;
    }


}
