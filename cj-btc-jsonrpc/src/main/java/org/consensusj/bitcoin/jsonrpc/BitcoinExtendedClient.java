package org.consensusj.bitcoin.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import org.consensusj.bitcoin.json.pojo.AddressInfo;
import org.consensusj.bitcoin.json.pojo.LoadWalletResult;
import org.consensusj.bitcoin.json.pojo.Outpoint;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;
import org.bitcoinj.core.Block;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extended Bitcoin JSON-RPC Client with added convenience methods.
 *
 * This class adds extra methods that aren't 1:1 mappings to standard
 * Bitcoin API RPC methods, but are useful for many common use cases -- specifically
 * the ones we ran into while building integration tests.
 */
public class BitcoinExtendedClient extends BitcoinClient {
    private static final Logger log = LoggerFactory.getLogger(BitcoinExtendedClient.class);

    private static final BigInteger NotSoPrivatePrivateInt = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
    private static final String RegTestMiningAddressLabel = "RegTestMiningAddress";
    private /* lazy */ Address regTestMiningAddress;

    public final Coin stdTxFee = Coin.valueOf(10000);
    public final Coin stdRelayTxFee = Coin.valueOf(1000);
    public final Integer defaultMaxConf = 9999999;
    public final long stdTxFeeSatoshis = stdTxFee.getValue();

    public Coin getStdTxFee() {
        return stdTxFee;
    }

    public Coin getStdRelayTxFee() {
        return stdRelayTxFee;
    }

    public Integer getDefaultMaxConf() {
        return defaultMaxConf;
    }

    public BitcoinExtendedClient(SSLSocketFactory sslSocketFactory, NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(sslSocketFactory, netParams, server, rpcuser, rpcpassword);
    }

    public BitcoinExtendedClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        this((SSLSocketFactory) SSLSocketFactory.getDefault(), netParams, server, rpcuser, rpcpassword);
    }

    public BitcoinExtendedClient(URI server, String rpcuser, String rpcpassword) {
        this((SSLSocketFactory) SSLSocketFactory.getDefault(), null, server, rpcuser, rpcpassword);
    }

    public BitcoinExtendedClient(RpcConfig config) {
        this(config.getNetParams(), config.getURI(), config.getUsername(), config.getPassword());
    }

    /**
     * Constructor that uses bitcoin.conf to get connection information (Incubating)
     */
    public BitcoinExtendedClient() {
        this(BitcoinConfFile.readDefaultConfig().getRPCConfig());
    }

    /**
     * Incubating: clone the client with a new base URI for a named wallet.
     * @param walletName wallet name
     * @param rpcUser username must be provided because it is not accessible in the parent class
     * @param rpcPassword password must be provided because it is not accessible in the parent class
     * @return A new client with a baseURI configured for the specified wallet name.
     */
    public BitcoinExtendedClient withWallet(String walletName, String rpcUser, String rpcPassword) {
        return new BitcoinExtendedClient(this.getNetParams(), this.getServerURI().resolve("/wallet/" + walletName), rpcUser, rpcPassword);
    }

    public synchronized Address getRegTestMiningAddress() {
        if (!getNetParams().getId().equals(NetworkParameters.ID_REGTEST)) {
            throw new UnsupportedOperationException("Operation only supported in RegTest context");
        }
        if (regTestMiningAddress == null) {
            // If in the future, we want to manage the keys for mined coins on the client side,
            // we could initialize regTestMiningKey from a bitcoinj-generated ECKey or HD Keychain.
            try {
                ECKey notSoPrivatePrivateKey = ECKey.fromPrivate(NotSoPrivatePrivateInt, false);
                Address address = Address.fromKey(RegTestParams.get(), notSoPrivatePrivateKey, Script.ScriptType.P2PKH);
                AddressInfo addressInfo = getAddressInfo(address);
                if (addressInfo.getIsmine() && !addressInfo.getIswatchonly() && addressInfo.getSolvable()) {
                    log.warn("Address with label {} is present in server-side wallet", RegTestMiningAddressLabel);
                    regTestMiningAddress = address;
                } else {
                    // Import known private key with label and rescan the blockchain for any transactions from
                    // previous test runs, rescan shouldn't take too long on a typical RegTest chain
                    log.warn("Adding private key for {} and rescanning chain", RegTestMiningAddressLabel);
                    importPrivKey(notSoPrivatePrivateKey, RegTestMiningAddressLabel, true);
                    regTestMiningAddress = address;
                }
                log.warn("Retrieved regTestMiningAddress = {}", regTestMiningAddress);
            } catch (IOException e) {
                log.error("Exception while checking/importing regTestMiningAddress", e);
                throw new RuntimeException(e);
            }
        }
        return regTestMiningAddress;
    }

    /**
     * Generate blocks and funds (RegTest only)
     *
     * Use this to generate blocks and receive the block reward in {@code this.regTestMiningAddress}
     * which can the be used to fund transactions in RegTest mode.
     *
     * @param numBlocks Number of blocks to mine
     * @return list of block hashes
     * @throws JsonRpcStatusException something broke
     * @throws IOException something broke
     */
    public List<Sha256Hash> generateBlocks(int numBlocks) throws JsonRpcStatusException, IOException {
        return this.generateToAddress(numBlocks, getRegTestMiningAddress());
    }

    /**
     * Returns information about a block at index provided.
     *
     * Use two RPCs: `getblockhash` and then `getblock`.
     * Note: somewhat-overrides (primitive vs boxed) deprecated method that will be removed.
     * The deprecated method in `BitcoinClient` takes an `Integer`, this method
     * takes an `int`.
     *
     * @param index The block index
     * @return The information about the block
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Block getBlock(int index) throws JsonRpcStatusException, IOException {
        Sha256Hash blockHash = getBlockHash(index);
        return getBlock(blockHash);
    }

    /**
     * Clears the memory pool and returns a list of the removed transactions.
     *
     * Note: this is a customized command, which is currently not part of Bitcoin Core.
     * See https://github.com/OmniLayer/OmniJ/pull/72[Pull Request #72] on GitHub
     *
     * @return A list of transaction hashes of the removed transactions
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<Sha256Hash> clearMemPool() throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, Sha256Hash.class);
        return send("clearmempool", resultType);
    }

    /**
     * Creates a raw transaction, spending from a single address, whereby no new change address is created, and
     * remaining amounts are returned to {@code fromAddress}.
     * <p>
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spend from
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     */
    public String createRawTransaction(Address fromAddress, Map<Address, Coin> outputs) throws JsonRpcStatusException, IOException {
        // Copy the Map (which may be immutable) and add prepare change output if needed.
        Map<Address, Coin> outputsWithChange = new HashMap<>(outputs);
        // Get unspent outputs via RPC
        List<UnspentOutput> unspentOutputs = listUnspent(0, defaultMaxConf, fromAddress);

        // Gather inputs as OutPoints
        List<Outpoint> inputs = unspentOutputs.stream()
                .map(this::unspentToOutpoint)
                .collect(Collectors.toList());

        // Calculate change
        final long amountIn = unspentOutputs
                .stream()
                .map(UnspentOutput::getAmount)
                .mapToLong(Coin::getValue)
                .sum();
        final long amountOut = outputs.values()
                .stream()
                .mapToLong(Coin::getValue)
                .sum();

        // Change is the difference less the standard transaction fee
        final long amountChange = amountIn - amountOut - stdTxFeeSatoshis;
        if (amountChange < 0) {
            // TODO: Throw Exception
            System.out.println("Insufficient funds"); // + ": ${amountIn} < ${amountOut + stdTxFee}"
        }
        if (amountChange > 0) {
            // Add a change output that returns change to sending address
            outputsWithChange.put(fromAddress, Coin.valueOf(amountChange));
        }

        // Call the server to create the transaction
        return createRawTransaction(inputs, outputsWithChange);
    }

    /**
     * Creates a raw transaction, sending {@code amount} from a single address to a destination, whereby no new change
     * address is created, and remaining amounts are returned to {@code fromAddress}.
     * <p>
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spent from
     * @param toAddress The destination
     * @param amount The amount
     * @return The hex-encoded raw transaction
     */
    public String createRawTransaction(Address fromAddress, Address toAddress, Coin amount) throws JsonRpcStatusException, IOException {
        return createRawTransaction(fromAddress, Map.of(toAddress, amount));
    }

    /**
     * Returns the Bitcoin balance of an address (in the server-side wallet.)
     *
     * @param address The address
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address) throws JsonRpcStatusException, IOException {
        // NOTE: because null is currently removed from the argument lists passed via RPC, using it here for default
        // values would result in the RPC call "listunspent" with arguments [["address"]], which is invalid, similar
        // to a call with arguments [null, null, ["address"]], as expected arguments are either [], [int], [int, int]
        // or [int, int, array]
        return getBitcoinBalance(address, 1, defaultMaxConf);
    }

    /**
     * Returns the Bitcoin balance of an address (in the server-side wallet) where spendable outputs have at least
     * {@code minConf} confirmations.
     *
     * @param address The address
     * @param minConf Minimum amount of confirmations
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address, Integer minConf) throws JsonRpcStatusException, IOException {
        return getBitcoinBalance(address, minConf, defaultMaxConf);
    }

    /**
     * Returns the Bitcoin balance of an address (in the server-side wallet) where spendable outputs have at least
     * {@code minConf} and not more than {@code maxConf} confirmations.
     *
     * @param address The address (must be in wallet)
     * @param minConf Minimum amount of confirmations
     * @param maxConf Maximum amount of confirmations
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address, Integer minConf, Integer maxConf) throws JsonRpcStatusException, IOException {
        return listUnspent(minConf, maxConf, address)
                .stream()
                .map(UnspentOutput::getAmount)
                .reduce(Coin.ZERO, Coin::add);
    }

    /**
     * Sends BTC from an address to a destination, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param toAddress   The destination address
     * @param amount      The amount to transfer
     * @return The transaction hash
     */
    public Sha256Hash sendBitcoin(Address fromAddress, Address toAddress, Coin amount) throws JsonRpcStatusException, IOException {
        Map<Address, Coin> outputs = Map.of(toAddress, amount);
        return sendBitcoin(fromAddress, outputs);
    }

    /**
     * Sends BTC from an address to the destinations, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param outputs     The destinations and amounts to transfer
     * @return The transaction hash
     */
    public Sha256Hash sendBitcoin(Address fromAddress, Map<Address, Coin> outputs) throws JsonRpcStatusException, IOException {
        String unsignedTxHex = createRawTransaction(fromAddress, outputs);
        SignedRawTransaction signingResult = signRawTransactionWithWallet(unsignedTxHex);

        Boolean complete = signingResult.isComplete();
        assert complete;

        String signedTxHex = signingResult.getHex();
        Sha256Hash txid = sendRawTransaction(signedTxHex);

        return txid;
    }

    /**
     * Create a signed transaction locally (i.e. with a client-side key.) Finds UTXOs this
     * key can spend (assuming they are Script.ScriptType.P2PKH UTXOs)
     *
     * @param fromKey Signing key
     * @param outputs Outputs to sign
     * @return A bitcoinj Transaction objects that is properly signed
     * @throws JsonRpcStatusException A JSON-RPC error was returned
     * @throws IOException            An I/O error occured
     */
    public Transaction createSignedTransaction(ECKey fromKey, List<TransactionOutput> outputs) throws JsonRpcStatusException, IOException {
        Address fromAddress = Address.fromKey(getNetParams(), fromKey, Script.ScriptType.P2PKH);

        Transaction tx = new Transaction(getNetParams());   // Create a new transaction
        outputs.forEach(tx::addOutput);                     // Add all requested outputs to it

        // Fetch all UTXOs for the sending Address
        List<TransactionOutput> unspentOutputs = listUnspentJ(fromAddress);

        // Calculate change (units are satoshis)
        // First sum all available UTXOs
        final Coin amountIn = unspentOutputs.stream()
                .map(TransactionOutput::getValue)
                .reduce(Coin.ZERO, Coin::add);
        // Then sum the requested outputs for this transaction
        final Coin amountOut = outputs.stream()
                .map(TransactionOutput::getValue)
                .reduce(Coin.ZERO, Coin::add);
        // Change is the difference less the standard transaction fee
        final long amountChange = amountIn.value - amountOut.value - stdTxFeeSatoshis;
        if (amountChange < 0) {
            // TODO: Throw Exception
            System.out.println("Insufficient funds"); // + ": ${amountIn} < ${amountOut + stdTxFeeSatoshis}"
        }
        if (amountChange > 0) {
            // Add a change output that returns change to sending address
            tx.addOutput(Coin.valueOf(amountChange), fromAddress);
        }

        // Add *all* UTXOs for fromAddress as inputs (this perhaps unnecessarily consolidates coins, with
        // a higher tx fee and some loss of privacy) and sign them
        unspentOutputs.forEach(unspent -> tx.addSignedInput(unspent, fromKey));
        return tx;
    }

    /**
     * Create a signed transaction locally (i.e. with a client-side key.) Finds UTXOs this
     * key can spend (assuming they are Script.ScriptType.P2PKH UTXOs)
     *
     * @param fromKey   Signing key
     * @param toAddress Destination address
     * @param amount    Amount to send
     * @return A bitcoinj Transaction objects that is properly signed
     * @throws JsonRpcStatusException A JSON-RPC error was returned
     * @throws IOException            An I/O error occured
     */
    public Transaction createSignedTransaction(ECKey fromKey, Address toAddress, Coin amount) throws JsonRpcStatusException, IOException {
        List<TransactionOutput> outputs = List.of(
                new TransactionOutput(getNetParams(), null, amount, toAddress));
        return createSignedTransaction(fromKey, outputs);
    }

    /**
     * Build a list of bitcoinj {@link TransactionOutput}s using {@link BitcoinClient#listUnspent}
     * and {@link BitcoinClient#getRawTransaction} RPCs.
     *
     * @param fromAddress Address to get UTXOs for
     * @return All unspent TransactionOutputs for fromAddress
     */
    public List<TransactionOutput> listUnspentJ(Address fromAddress) throws JsonRpcStatusException, IOException {
        List<UnspentOutput> unspentOutputs = listUnspent(0, defaultMaxConf, fromAddress); // RPC UnspentOutput objects
        return unspentOutputs.stream()
                .map(this::unspentToTransactionOutput)
                .collect(Collectors.toList());
    }

    /**
     * Build a list of bitcoinj {@link TransactionOutPoint}s using {@link BitcoinClient#listUnspent}.
     *
     * @param fromAddress Address to get UTXOs for
     * @return All unspent TransactionOutPoints for fromAddress
     */
    public List<TransactionOutPoint> listUnspentOutPoints(Address fromAddress) throws JsonRpcStatusException, IOException {
        List<UnspentOutput> unspentOutputsRPC = listUnspent(0, defaultMaxConf, fromAddress); // RPC UnspentOutput objects
        return unspentOutputsRPC.stream()
                .map(this::unspentToTransactionOutpoint)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert an {@link UnspentOutput} JSONRPC-POJO to a *bitcoinj* {@link TransactionOutput} (that's out-PUT).
     * Calls {@link BitcoinClient#getRawTransaction(Sha256Hash)}
     * for every item. Throws {@link RuntimeException} if any of those RPC calls fails.
     *
     * @param unspentOutput The input POJO
     * @return The *bitcoinj* object  (that's out-PUT)
     */
    private TransactionOutput unspentToTransactionOutput(UnspentOutput unspentOutput) {
        try {
            return getRawTransaction(unspentOutput.getTxid())
                    .getOutput(unspentOutput.getVout());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an {@link UnspentOutput} JSONRPC-POJO to a *bitcoinj* {@link TransactionOutPoint} (that's out-POINT).
     *
     * @param unspentOutput The input POJO
     * @return The *bitcoinj* object  (that's out-POINT)
     */
    private TransactionOutPoint unspentToTransactionOutpoint(UnspentOutput unspentOutput) {
        return new TransactionOutPoint(getNetParams(), unspentOutput.getVout(), unspentOutput.getTxid());
    }

    /**
     * Convert an {@link UnspentOutput} JSONRPC-POJO to a JSONRPC-POJO {@link Outpoint} .
     *
     * @param unspentOutput The input UnspentOutput POJO
     * @return the Outpoint POJO
     */
    private Outpoint unspentToOutpoint(UnspentOutput unspentOutput) {
        return new Outpoint(unspentOutput.getTxid(), unspentOutput.getVout());
    }
}
