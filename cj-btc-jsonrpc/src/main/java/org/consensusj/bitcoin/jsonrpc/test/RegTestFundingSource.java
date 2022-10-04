package org.consensusj.bitcoin.jsonrpc.test;

import org.consensusj.bitcoin.json.pojo.LoadWalletResult;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.bitcoin.json.pojo.Outpoint;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public RegTestFundingSource(BitcoinExtendedClient client) {
        this.client = client;
    }

    /**
     * Generate blocks (if necessary) and fund an address with requested amount of BTC.
     * This will create, sign, and transmit a transaction to fund an address with BTC. This method
     * does not mine a block to confirm the transaction. A separate call to {@link BitcoinExtendedClient#generateBlocks(int)}
     * or equivalent must be made to confirm the transaction.
     *
     * @param toAddress Address to fund with BTC
     * @param requestAmount Amount of BTC to "mine" and send (minimum ending balance of toAddress?)
     * @return The hash of an unconfirmed transaction that provides the funds.
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
     * Create an address and fund it with bitcoin.
     * This will create, sign, and transmit a transaction to fund an address with BTC. This method
     * does not mine a block to confirm the transaction. A separate call to {@link BitcoinExtendedClient#generateBlocks(int)}
     * or equivalent must be made to confirm the transaction.
     *
     * @param amount requested amount of BTC
     * @return Newly created address that will have the requested amount of bitcoin after the next block is mined
     */
    @Override
    public Address createFundedAddress(Coin amount) throws Exception {
        Address address = client.getNewAddress();
        requestBitcoin(address, amount);
        return address;
    }

    /**
     * Mine zero or more blocks until {@code availableFunds >= requestedAmount + fee}
     *
     * @param requestAmount Funds requested
     * @return A list of unspent outputs
     * @throws IOException ISH
     */
    private List<UnspentOutput> mineEnoughFunds(Coin requestAmount) throws IOException {
        Coin neededAmount = requestAmount.plus(txFee);

        List<UnspentOutput> available = availableFunds();

        while (sumUnspentOutputs(available).isLessThan(neededAmount)) {
            client.generateBlocks(1);
            available = availableFunds();
            logMined(neededAmount, available);
        }

        log.info("mineEnoughFunds: Needed: {} Returned: {}", neededAmount.toPlainString(), sumUnspentOutputs(available).toPlainString());

        return available;
    }

    private void logMined(Coin needed, List<UnspentOutput> available) throws IOException {
        int height = client.getBlockCount();
        log.warn("⛏⛏⛏⛏⛏ Mined {} (blk#{}): Available: {} Needed: {} ⛏⛏⛏⛏⛏",
                rewardFromRegTestHeight(height).toPlainString(),
                height,
                sumUnspentOutputs(available).toPlainString(),
                needed.toPlainString());
    }

    private Coin rewardFromRegTestHeight(int height) {
        int halvings = height / 150;
        return Coin.valueOf(Coin.FIFTY_COINS.value >> halvings);
    }

    private Map<Address, Coin> calcChange(Coin availableFunds, Coin amountToSend, Address destAddress, Address changeAddress) {
        Coin change = availableFunds.minus(amountToSend.plus(txFee));
        return change.isPositive()
                ? Map.of(destAddress, amountToSend, changeAddress, change)  // include change output
                : Map.of(destAddress, amountToSend);                        // no change output
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
        Map<Address,Coin> outputs = Map.of(client.getRegTestMiningAddress(), amountIn.subtract(txFee));

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
        return client.listUnspent(1, defaultMaxConf, null, null);
    }

    /**
     * Get all available funds in a specific address in the server-side wallet
     */
    private List<UnspentOutput> getSpendable(Address address) throws  JsonRpcException, IOException {
        return client.listUnspent(1, defaultMaxConf, address)
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
        return unspentOutputs.stream()
                .map(UnspentOutput::getAmount)
                .reduce(Coin.ZERO, Coin::add);
    }

    private Sha256Hash sendRawTransactionUnlimitedFees(String hexTx) throws IOException {
        return client.sendRawTransaction(hexTx, Coin.ZERO);
    }
}
