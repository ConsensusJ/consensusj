package com.msgilligan.bitcoinj.test;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import org.consensusj.jsonrpc.JsonRpcException;
import com.msgilligan.bitcoinj.json.pojo.Outpoint;
import com.msgilligan.bitcoinj.json.pojo.SignedRawTransaction;
import com.msgilligan.bitcoinj.json.pojo.UnspentOutput;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FundingSource using RegTest mining, BitcoinExtendedClient (with getRegTestMiningAddress),
 * and the server's default wallet for accumulating coins.
 */
public class RegTestFundingSource implements FundingSource {
    private final Coin txFee = Coin.valueOf(200_000);
    private final Integer defaultMaxConf = 9999999;
    private static final Logger log = LoggerFactory.getLogger(RegTestFundingSource.class);
    protected final BitcoinExtendedClient client;
    protected final int bitcoinCoreVersion;

    public RegTestFundingSource(BitcoinExtendedClient client) {
        this.client = client;
        try {
            bitcoinCoreVersion = client.getNetworkInfo().getVersion();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // Bitcoin Core 0.21+ will not create default wallet (named "") if it doesn't exist
        // so we have to do it ourselves
        if (bitcoinCoreVersion >= 210000) {
            try {
                List<String> walletList = client.listWallets();
                if (!walletList.contains("")) {
                    Map<String, String> result = client.createWallet("", false, false);
                    log.warn("Created default wallet: {}", result);
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    /**
     * Generate blocks and fund an address with requested amount of BTC
     *
     * TODO: test new logic and delete "old logic"
     *
     * @param toAddress Address to fund with BTC
     * @param requestAmount Amount of BTC to "mine" and send (minimum ending balance of toAddress?)
     * @return The hash of transaction that provided the funds.
     */
    @Override
    public Sha256Hash requestBitcoin(Address toAddress, Coin requestAmount) throws JsonRpcException, IOException {
        log.info("requestBitcoin requesting {} for {}", requestAmount.toPlainString(), toAddress);
        if (requestAmount.value > NetworkParameters.MAX_MONEY.value) {
            throw new IllegalArgumentException("request exceeds MAX_MONEY");
        }

        // Generate blocks until regTestMiningAddress has enough funds
        List<UnspentOutput> unspent = mineEnoughFunds(requestAmount);
        List<Outpoint> inputs = unspentOutputsToOutpoints(unspent);
        Coin availableToSpend = sumUnspentOutputs(unspent);

        // Create the funding transaction
        Map<Address, Coin> outputs = calcChange(availableToSpend, requestAmount, toAddress, client.getRegTestMiningAddress());
        String unsignedTxHex = client.createRawTransaction(inputs, outputs);

        // Sign the funding transaction
        SignedRawTransaction signingResult = client.signRawTransactionWithWallet(unsignedTxHex);
        if (!signingResult.isComplete()) {
            log.error("Unable to complete signing!");
            log.error("SigningResult: {}", toJson(signingResult));
        }
        assert signingResult.isComplete();

        // Send the funding transaction
        Sha256Hash txid = sendRawTransactionUnlimitedFees(signingResult.getHex());
        log.info("Funding transaction sent: {}", txid);

        return txid;
    }

    /**
     * Mine zero or more blocks until {@code availableFunds >= requestedAmount}
     *
     * @param requestAmount Funds requested
     * @return A list of unspent outputs
     * @throws IOException ISH
     */
    private List<UnspentOutput> mineEnoughFunds(Coin requestAmount) throws IOException {
        List<UnspentOutput> unspent = availableFunds();
        Coin availableAmount = sumUnspentOutputs(unspent);
        log.info("mineEnoughFunds: Available: {} Requested: {}", availableAmount.toPlainString(), requestAmount.toPlainString());

        while (availableAmount.isLessThan(requestAmount.plus(txFee))) {
            client.generateToAddress(1, client.getRegTestMiningAddress());
            unspent = availableFunds();
            availableAmount = sumUnspentOutputs(unspent);
            int height = client.getBlockCount();
            log.warn("⛏⛏⛏⛏⛏ Mined {} (blk#{}): Available: {} Requested: {} ⛏⛏⛏⛏⛏",
                    rewardFromRegTestHeight(height).toPlainString(),
                    height,
                    availableAmount.toPlainString(),
                    requestAmount.toPlainString());
        }
        return unspent;
    }

    private Coin rewardFromRegTestHeight(int height) {
        int halvings = height / 150;
        return Coin.valueOf(Coin.FIFTY_COINS.value >> halvings);
    }

    private Map<Address, Coin> calcChange(Coin availableFunds, Coin amountToSend, Address destAddress, Address changeAddress) {
        Coin change;
        if (availableFunds.value - (amountToSend.value + txFee.value) > 0) {
            change = Coin.valueOf(availableFunds.value - (amountToSend.value + txFee.value));
        } else {
            change = Coin.ZERO;
        }
        Map<Address, Coin> outputs = change.value > 0 ?
                // Send change to regTestMiningAddress
                mapOf(destAddress, amountToSend, client.getRegTestMiningAddress(), change) :
                // No change, send everything
                Collections.singletonMap(destAddress, amountToSend);
        return outputs;
    }

    // This can be eliminated when we upgrade this file to Java 9
    private <K,V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V>  outputs = new HashMap<>();
        outputs.put(k1, v1);
        outputs.put(k2, v2);
        return outputs;
    }

    /**
     * Create an address and fund it with bitcoin
     *
     * @param amount requested amount
     * @return Newly created address with the requested amount of bitcoin
     */
    @Override
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
            log.error("exception: ", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("exception: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Collects *all* unspent outputs and sends to the RegTestMiningAddress. No new block is generated afterwards.
     *
     * NOTE: This consolidates all coins in the wallet to the RegTestMiningAddress and should only be run
     * at the end of a test (if you are using wallet addresses in your test)
     *
     * Can be used in cleanupSpec() methods of integration tests.
     *
     * @see <a href="https://github.com/OmniLayer/OmniJ/issues/50">Issue #50 on GitHub</a>
     */
    void consolidateCoins() throws JsonRpcException, IOException {
        // Get all UTXOs in the servers wallet
        List<UnspentOutput> unspentOutputs = getSpendable();

        // Check if the amount is large enough to be worth consolidating
        Coin amountIn = sumUnspentOutputs(unspentOutputs);
        log.info("We have {} in {} utxos.", amountIn.toPlainString(), unspentOutputs.size());
        if (unspentOutputs.size() < 10 || amountIn.value <= 10 * txFee.value) {
            log.info("Not consolidating.");
            return;
        }

        // Gather inputs
        List<Outpoint> inputs = unspentOutputsToOutpoints(unspentOutputs);

        // Send it all to the RegTestMiningAddress
        Map<Address,Coin> outputs = Collections.singletonMap(client.getRegTestMiningAddress(), amountIn.subtract(txFee));

        String unsignedTxHex = client.createRawTransaction(inputs, outputs);
        SignedRawTransaction signingResult = client.signRawTransactionWithWallet(unsignedTxHex);

        boolean complete = signingResult.isComplete();
        if (!complete) {
            log.error("Unable to complete signing on consolidate coins transaction.");
            log.error("SigningResult: {}", toJson(signingResult));
        }
        assert complete;

        String signedTxHex = signingResult.getHex();
        Sha256Hash txid = sendRawTransactionUnlimitedFees(signedTxHex);
        log.warn("⭄⭄⭄⭄⭄⭄⭄ Consolidating transaction sent, txid = {}", txid);
    }

    private String toJson(SignedRawTransaction signingResult) {
        return client.getMapper().valueToTree(signingResult).toPrettyString();
    }

    /**
     * Get available funds in the RegTestMiningAddress
     */
    private List<UnspentOutput> availableFunds() throws IOException {
        return getSpendable(client.getRegTestMiningAddress());
    }

    /**
     * Get all available funds in the server-side wallet
     */
    private List<UnspentOutput> getSpendable() throws  JsonRpcException, IOException {
        return client.listUnspent(1, defaultMaxConf, null);
    }

    /**
     * Get all available funds in a specific address in the server-side wallet
     */
    private List<UnspentOutput> getSpendable(Address address) throws  JsonRpcException, IOException {
        return client.listUnspent(1, defaultMaxConf, Collections.singletonList(address))
                .stream()
                .filter(out -> out.isSpendable() && out.isSafe())
                .collect(Collectors.toList());
    }

    private List<Outpoint> unspentOutputsToOutpoints(List<UnspentOutput> unspentOutputs) {
        return unspentOutputs.stream()
                .map(output -> new Outpoint(output.getTxid(), output.getVout()))   // map from UnspentOutput to Outpoint
                .collect(Collectors.toList());
    }

    private Coin sumUnspentOutputs(List<UnspentOutput> unspentOutputs) {
        return Coin.valueOf(unspentOutputs.stream().mapToLong(output -> output.getAmount().value).sum());
    }

    private Sha256Hash sendRawTransactionUnlimitedFees(String hexTx) throws IOException {
        return client.sendRawTransaction(hexTx, Coin.ZERO);
    }
}
