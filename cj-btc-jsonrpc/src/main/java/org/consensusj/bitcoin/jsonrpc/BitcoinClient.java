package org.consensusj.bitcoin.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.AddressGroupingItem;
import org.consensusj.bitcoin.json.pojo.AddressInfo;
import org.consensusj.bitcoin.json.pojo.BitcoinTransactionInfo;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.json.pojo.LoadWalletResult;
import org.consensusj.bitcoin.json.pojo.MethodHelpEntry;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.json.pojo.Outpoint;
import org.consensusj.bitcoin.json.pojo.RawTransactionInfo;
import org.consensusj.bitcoin.json.pojo.ReceivedByAddressInfo;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.json.pojo.TxOutInfo;
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo;
import org.consensusj.bitcoin.json.pojo.UnloadWalletResult;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;
import org.consensusj.bitcoin.json.conversion.RpcClientModule;
import org.consensusj.bitcoin.json.pojo.WalletTransactionInfo;
import org.consensusj.bitcoin.json.pojo.ZmqNotification;
import org.consensusj.bitcoin.json.pojo.bitcore.AddressBalanceInfo;
import org.consensusj.bitcoin.json.pojo.bitcore.AddressRequest;
import org.consensusj.bitcoin.json.pojo.bitcore.AddressUtxoInfo;
import org.consensusj.bitcoin.jsonrpc.internal.BitcoinClientThreadFactory;
import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcErrorException;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.consensusj.jsonrpc.JsonRpcClientHttpUrlConnection;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JSON-RPC Client for <b>Bitcoin Core</b>.
 * <p>
 * A strongly-typed wrapper for a Bitcoin Core JSON-RPC client using the
 * <a href="https://bitcoincore.org/en/doc/">Bitcoin Core JSON-RPC API</a>.
 * <p>
 * <a href="https://bitcoinj.org">bitcoinj</a> types are used where appropriate.
 * For example, requesting a block hash will return a {@link org.bitcoinj.core.Sha256Hash}:
 *
 * <pre> {@code
 * Sha256Hash hash = client.getBlockHash(342650);
 * }</pre>
 *
 * Requesting a Bitcoin balance will return the amount as a {@link org.bitcoinj.core.Coin}:
 *
 * <pre> {@code
 * Coin balance = client.getBalance();
 * }</pre>
 *
 * This version is written to be compatible with Bitcoin Core 0.20 and later. If used with
 * Omni Core (an enhanced version of Bitcoin Core with Omni Protocol support) Omni Core 0.11.0 or later is required.
 * <p>
 * Note that according to <a href="https://github.com/bitcoin/bitcoin/issues/2960">Issue #2960: Support JSON-RPC 2.0</a> Bitcoin Core
 * does not correctly follow the JSON-RPC 2.0 specification.
 * @see org.consensusj.jsonrpc.JsonRpcClient
 * @see <a href="https://bitcoincore.org/en/doc/">Bitcoin Core JSON-RPC API reference</a>
 * @see <a href="https://github.com/bitcoin/bitcoin/issues/2960">Bitcoin Core Issue #2960: Support JSON-RPC 2.0</a>
 * <p>
 * <b>This is still a work-in-progress and the API will change.</b>
 *
 */
public class BitcoinClient extends JsonRpcClientHttpUrlConnection implements ChainTipClient {
    private static final Logger log = LoggerFactory.getLogger(BitcoinClient.class);

    private static final int THREAD_POOL_SIZE = 5;

    private static final int SECOND_IN_MSEC = 1000;
    private static final int RETRY_SECONDS = 5;
    private static final int MESSAGE_SECONDS = 30;

    // TODO: Replace NetworkParameters with Network/BitcoinNetwork once we upgrade to bitcoinj 0.17 (once it is released)
    private NetworkParameters netParams;
    private ExecutorService executorService;

    private int serverVersion = 0;    // 0 means unknown serverVersion
    private boolean isAddressIndexSuccessfullyTested = false;
    private boolean isAddressIndexEnabled;

    public BitcoinClient(SSLSocketFactory sslSocketFactory, NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(sslSocketFactory, JsonRpcMessage.Version.V2, server, rpcuser, rpcpassword);
        this.netParams = netParams;
        if (netParams != null) {
            initExecutor();
        }
    }

    private void initExecutor() {
        mapper.registerModule(new RpcClientModule(netParams));
        ThreadFactory threadFactory = new BitcoinClientThreadFactory(Context.getOrCreate(netParams), "Bitcoin RPC Client");
        // TODO: Tune and/or make configurable the thread pool size.
        // Current pool size of 5 is chosen to minimize simultaneous active RPC
        // calls in `bitcoind` -- which is not designed for serving multiple clients.
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);
    }

    // TODO: Reconcile this constructor mode with {@link #waitForServer(int)}
    /**
     * Incubating constructor that doesn't require a {@link NetworkParameters}.
     * <p>
     * When using this constructor, it is recommended that {@link #getNetParams()} be called after construction
     * and before any other methods are called, to allow the Bitcoin network type to be initialized.
     * @param sslSocketFactory Custom socket factory
     * @param server URI of the Bitcoin RPC server
     * @param rpcuser Username (if required)
     * @param rpcpassword Password (if required)
     */
    public BitcoinClient(SSLSocketFactory sslSocketFactory, URI server, String rpcuser, String rpcpassword) {
        this(sslSocketFactory, null, server, rpcuser, rpcpassword);
    }

    /**
     * Incubating constructor that doesn't require a {@link NetworkParameters}.
     * <p>
     * When using this constructor, it is recommended that {@link #getNetParams()} be called after construction
     * and before any other methods are called, to allow the Bitcoin network type to be initialized.
     * @param server URI of the Bitcoin RPC server
     * @param rpcuser Username (if required)
     * @param rpcpassword Password (if required)
     */
    public BitcoinClient(URI server, String rpcuser, String rpcpassword) {
        this((SSLSocketFactory) SSLSocketFactory.getDefault(), null, server, rpcuser, rpcpassword);
    }

    /**
     * Construct a BitcoinClient from Network Parameters, URI, user name, and password.
     * @param netParams Correct Network Parameters for destination server
     * @param server URI of the Bitcoin RPC server
     * @param rpcuser Username (if required)
     * @param rpcpassword Password (if required)
     */
    public BitcoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        this((SSLSocketFactory) SSLSocketFactory.getDefault(), netParams, server, rpcuser, rpcpassword);
    }

    /**
     * Construct a BitcoinClient from an RPCConfig data object.
     * @param config Contains URI, user name, and password
     */
    public BitcoinClient(RpcConfig config) {
        this(config.getNetParams(), config.getURI(), config.getUsername(), config.getPassword());
    }

    /**
     * Get network parameters
     * <p>
     * Traditionally the Bitcoin network has been required as a constructor parameter and required to match
     * the mode of the server. However, to simplify client configuration we have added a constructor
     * that doesn't require a {@link NetworkParameters}. This changes some assumptions about how {@code BitcoinClient} works.
     * Previously, no JSON-RPC I/O calls would be performed unless something was explicitly requested -- which
     * also gave users of {@code BitcoinClient} the ability to call {@link #waitForServer(int)}
     * before calling any RPCs.
     * <p>
     * Until further improvements/changes are made, if you use one of the constructors that does not specify a
     * {@code NetworkParameters} you should call {@code getNetParams()} as soon as possible after calling the constructor
     * (especially before calling any JSON-RPC I/O methods except {@link #waitForServer(int)}).
     * @return network parameters for the server
     */
    public synchronized NetworkParameters getNetParams() {
        if (netParams == null) {
            netParams = getNetworkFromServer().join();
            initExecutor();
        }
        return netParams;
    }

    @Override
    public ExecutorService getDefaultAsyncExecutor() {
        return executorService;
    }
    
    /**
     * Shutdown our thread pool, etc.
     *
     * @throws InterruptedException if one happens
     */
    @Override
    public void close() throws InterruptedException {
        // TODO: See shutdownAndAwaitTermination method in the ExecutorService JavaDoc for
        // how to correctly implement this.
        executorService.shutdown();
        boolean successfullyTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
        if (!successfullyTerminated) {
            log.warn("timeout while closing");
        }
    }

    /**
     * Get a (cached after first call) serverVersion number
     * @return serverVersion number of bitcoin node
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    synchronized int getServerVersion() throws IOException, JsonRpcStatusException {
        if (serverVersion == 0) {
            serverVersion = getNetworkInfo().getVersion();
        }
        return serverVersion;
    }

    // TODO: Convert to {@code Network} type with bitcoinj 0.17
    private CompletableFuture<NetworkParameters> getNetworkFromServer() {
        return getBlockchainInfoMap().thenApply(info -> {
            NetworkParameters params;
            switch((String) info.get("chain")) {
                case "main":
                    params = MainNetParams.get();
                    break;
                case "test":
                    params = TestNet3Params.get();
                    break;
                // TODO: Signet support?
                case "regtest":
                    params = RegTestParams.get();
                    break;
                default:
                    throw new RuntimeException("Server returned unrecognized Bitcoin network");

            }
            return params;
        });
    }

    /**
     * Return BlockchainInfo as a Map (this avoids use of RpcClientModule and a circular dependency on NetworkParameters)
     * Note that we also can't use our ThreadFactory or ExecutorService because they depend on NetworkParameters as well,
     * and are not created until after we know what network we are on.
     * @return  getblockchaininfo response JSON as a Map
     */
    private CompletableFuture<Map<String, Object>> getBlockchainInfoMap() {
        return supplyAsync(() -> send("getblockchaininfo"), r -> new Thread(r).start());
    }

    /**
     * Test if address index is enabled, caching the result of the first successful check
     *
     * @return true if enabled, false if not enabled
     * @throws JsonRpcException an exception other than 1 of the two expected exceptions is thrown
     * @throws IOException something else went wrong
     */
    public synchronized boolean isAddressIndexEnabled() throws JsonRpcException, IOException {
        if (!isAddressIndexSuccessfullyTested) {
            try {
                AddressBalanceInfo info = getAddressBalance(getTestAddress());
                isAddressIndexSuccessfullyTested = true;
                isAddressIndexEnabled = true;
            } catch (JsonRpcErrorException ee) {
                // If method not found, the method we use for the test isn't even present, so definitely
                // no address index support is available
                if (ee.getError().getCode() == JsonRpcError.Error.METHOD_NOT_FOUND.getCode()) {
                    isAddressIndexSuccessfullyTested = true;
                    isAddressIndexEnabled = false;
                } else {
                    // Some other, unexpected exception, throw it
                    throw ee;
                }
            } catch (JsonRpcStatusException se) {
                // If the method is there and it returns an error of "Address index not enabled" then we also
                // know that no address index support is available
                if (se.getMessage().equals("Address index not enabled")) {
                    isAddressIndexSuccessfullyTested = true;
                    isAddressIndexEnabled = false;
                } else if ( se.jsonRpcCode == JsonRpcError.Error.METHOD_NOT_FOUND.getCode()) {
                    // It looks like the behavior changed in Bitcoin Core v23.0 and we end up here rather than in the  JsonRpcErrorException handler above
                    isAddressIndexSuccessfullyTested = true;
                    isAddressIndexEnabled = false;
                } else {
                    // Some other, unexpected exception, throw it
                    throw se;
                }
            }
        }
        return isAddressIndexEnabled;
    }

    private Address getTestAddress() {
        switch (getNetParams().getId()) {
            case NetworkParameters.ID_MAINNET:
                return LegacyAddress.fromBase58(null, "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
            case NetworkParameters.ID_TESTNET:
            case NetworkParameters.ID_REGTEST:
            default:
                return LegacyAddress.fromBase58(null, "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP");
        }
    }

    // TODO: Make this an async method that returns CompletableFuture.
    // TODO: Consider renaming this method "connect"
    // TODO: Consider having this method automatically get the Bitcoin Network, Server Version, RegTest mining address, etc once connection is made
    /**
     * Wait until the server is available.
     *
     * Keep trying, ignoring (and logging) a known list of exception conditions that may occur while waiting for
     * a `bitcoind` server to start up. This is similar to the behavior enabled by the `-rpcwait`
     * option to the `bitcoin-cli` command-line tool.
     *
     * @param timeout Timeout in seconds
     * @return true if ready, false if timeout or interrupted
     * @throws JsonRpcException if an "unexpected" exception happens (i.e. an error other than what happens during normal server startup)
     */
    public Boolean waitForServer(int timeout) throws JsonRpcException {

        log.debug("Waiting for server RPC ready...");

        String status;          // Status message for logging
        String statusLast = null;
        int seconds = 0;
        while (seconds < timeout) {
            try {
                Integer block = this.getBlockCount();
                if (block != null) {
                    log.debug("RPC Ready.");
                    return true;
                }
                status = "getBlock returned null";
            } catch (SocketException se) {
                // These are expected exceptions while waiting for a server
                if (se.getMessage().equals("Unexpected end of file from server") ||
                        se.getMessage().equals("Connection reset") ||
                        se.getMessage().contains("Connection refused") ||
                        se.getMessage().equals("recvfrom failed: ECONNRESET (Connection reset by peer)")) {
                    status = se.getMessage();
                } else {
                    throw new JsonRpcException("Unexpected exception in waitForServer", se) ;
                }

            } catch (EOFException ignored) {
                /* Android exception, ignore */
                // Expected exceptions on Android, RoboVM
                status = ignored.getMessage();
            } catch (JsonRpcStatusException e) {
                // If server is in "warm-up" mode, e.g. validating/parsing the blockchain...
                if (e.jsonRpcCode == -28) {
                    // ...then grab text message for status logging
                    status = e.getMessage();
                } else {
                    log.error("Rethrowing JsonRpcStatusException: ", e);
                    throw e;
                }
            } catch (IOException e) {
                // Ignore all IOExceptions
                status = e.getMessage();
            }
            try {
                // Log status messages only once, if new or updated
                if (!status.equals(statusLast)) {
                    log.info("Waiting for server: RPC Status: " + status);
                    statusLast = status;
                }
                Thread.sleep(RETRY_SECONDS * SECOND_IN_MSEC);
                seconds += RETRY_SECONDS;
            } catch (InterruptedException e) {
                log.error(e.toString());
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.error("waitForServer() timed out after {} seconds.", timeout);
        return false;
    }

    /**
     * Wait for RPC server to reach specified block height.
     *
     * @param blockHeight Block height to wait for
     * @param timeout     Timeout in seconds
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     * @return True if blockHeight reached, false if timeout or interrupted
     */
    public Boolean waitForBlock(int blockHeight, int timeout) throws JsonRpcStatusException, IOException {

        log.info("Waiting for server to reach block " + blockHeight);

        int seconds = 0;
        while (seconds < timeout) {
            Integer block = this.getBlockCount();
            if (block >= blockHeight) {
                log.info("Server is at block " + block + " returning 'true'.");
                return true;
            } else {
                try {
                    if (seconds % MESSAGE_SECONDS == 0) {
                        log.debug("Server at block " + block);
                    }
                    Thread.sleep(RETRY_SECONDS * SECOND_IN_MSEC);
                    seconds += RETRY_SECONDS;
                } catch (InterruptedException e) {
                    log.error(e.toString());
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        log.error("Timeout waiting for block " + blockHeight);
        return false;
    }

    /**
     * Returns the number of blocks in the longest block chain.
     *
     * @return The current block count
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Integer getBlockCount() throws JsonRpcStatusException, IOException {
        return send("getblockcount");
    }

    /**
     * Returns the hash of block in best-block-chain at index provided.
     *
     * @param index The block index
     * @return The block hash
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Sha256Hash getBlockHash(Integer index) throws JsonRpcStatusException, IOException {
        return send("getblockhash", Sha256Hash.class, index);
    }

    /**
     * Returns information about a block with the given block hash.
     *
     * @param hash The block hash
     * @return The information about the block (JSON/POJO object)
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public BlockInfo getBlockInfo(Sha256Hash hash) throws JsonRpcStatusException, IOException {
        // Use "verbose = true"
        return send("getblock", BlockInfo.class, hash, true);
    }

    /**
     * Returns information about a block with the given block hash.
     *
     * @param hash The block hash
     * @return The information about the block (bitcoinj {@link Block} object)
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Block getBlock(Sha256Hash hash) throws JsonRpcStatusException, IOException {
        // Use "verbose = false"
        return send("getblock", Block.class, hash, false);
    }

    /**
     * Mine blocks immediately (RegTest mode)
     * @since Bitcoin Core 0.13.0
     *
     * @param numBlocks Number of blocks to mine
     * @param address Address to send mined coins to
     * @param maxtries How many iterations to try (or null to use server default -- currently 1,000,000)
     * @return list containing block header hashes of the generated blocks
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<Sha256Hash> generateToAddress(int numBlocks, Address address, Integer maxtries) throws IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, Sha256Hash.class);
        return send("generatetoaddress", resultType, (long) numBlocks, address, maxtries);
    }

    /**
     * Mine blocks immediately (RegTest mode)
     * @since Bitcoin Core 0.13.0
     *
     * @param numBlocks Number of blocks to mine
     * @param address Address to send mined coins to
     * @return list containing block header hashes of the generated blocks
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<Sha256Hash> generateToAddress(int numBlocks, Address address) throws IOException {
        return generateToAddress(numBlocks,address, null);
    }

    public List<String> listWallets() throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        return send("listwallets", resultType);
    }

    public LoadWalletResult createWallet(String name, Boolean disablePrivateKeys, Boolean blank) throws JsonRpcStatusException, IOException {
        return createWallet(name, disablePrivateKeys, blank, null, null, null, null, null);
    }

    public LoadWalletResult createWallet(String name, Boolean disablePrivateKeys, Boolean blank, String passPhrase,
                                         Boolean avoidReuse) throws JsonRpcStatusException, IOException {
        return send("createwallet", LoadWalletResult.class, name, disablePrivateKeys, blank, passPhrase, avoidReuse);
    }

    // To use all these params server must be "recent"
    public LoadWalletResult createWallet(String name, Boolean disablePrivateKeys, Boolean blank, String passPhrase,
                                         Boolean avoidReuse, Boolean descriptors, Boolean loadOnStartup,
                                         Boolean externalSigner) throws JsonRpcStatusException, IOException {
        return send("createwallet", LoadWalletResult.class, name, disablePrivateKeys, blank, passPhrase, avoidReuse, descriptors, loadOnStartup, externalSigner);
    }

    public UnloadWalletResult unloadWallet() throws JsonRpcStatusException, IOException {
        return unloadWallet(null, null);
    }

    public UnloadWalletResult unloadWallet(String name, Boolean loadOnStartup) throws JsonRpcStatusException, IOException {
        return send("unloadwallet", UnloadWalletResult.class, name, loadOnStartup);
    }

    /**
     * Creates a new Bitcoin address for receiving payments, linked to the default account "".
     *
     * @return A new Bitcoin address
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Address getNewAddress() throws JsonRpcStatusException, IOException {
        return getNewAddress(null);
    }

    /**
     * Creates a new Bitcoin address for receiving payments.
     *
     * @param label The label name for the address to be linked to.
     * @return A new Bitcoin address
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Address getNewAddress(String label) throws JsonRpcStatusException, IOException {
        return send("getnewaddress", Address.class, label);
    }
    
    /**
     * Return the private key from the server.
     *
     * Note: must be in wallet mode with unlocked or unencrypted wallet.
     *
     * @param address Address corresponding to the private key to return
     * @return The private key
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public ECKey dumpPrivKey(Address address) throws JsonRpcStatusException, IOException {
        return send("dumpprivkey", ECKey.class, address);
    }

    /**
     * Import a private key into the server's wallet
     *
     * @param privateKey An ECKey (containing a private key)
     * @param label The server-side label for the key
     * @param rescan Rescan the blockchain to find transactions for this key
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public void importPrivKey(ECKey privateKey, String label, boolean rescan) throws JsonRpcStatusException, IOException {
        send("importprivkey", Void.class, privateKey.getPrivateKeyEncoded(this.getNetParams()).toBase58(), label, rescan);
    }
    
    /**
     * Creates a raw transaction spending the given inputs to the given destinations.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param inputs  The outpoints to spent
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public String createRawTransaction(List<Outpoint> inputs, Map<Address, Coin> outputs)
            throws JsonRpcStatusException, IOException {
        return send("createrawtransaction", inputs, outputs);
    }

    /**
     * Signs inputs of a raw transaction using the wallet. Arguments 2 and 3 of the RPC are currently
     * not supported, which means UTXOs not currently in the blockchain can't be used and `sighashtype`
     * defaults to `ALL`.
     *
     * @param unsignedTransaction The hex-encoded raw transaction
     * @return The signed transaction and information whether it has a complete set of signature
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     * @since Bitcoin Core v0.17
     */
    public SignedRawTransaction signRawTransactionWithWallet(String unsignedTransaction) throws JsonRpcStatusException, IOException {
        return send("signrawtransactionwithwallet", SignedRawTransaction.class, unsignedTransaction);
    }

    /**
     * Get a "raw" transaction (which we use to construct a bitcoinj {@code Transaction})
     * @param txid Transaction ID/hash
     * @return bitcoinj Transaction
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Transaction getRawTransaction(Sha256Hash txid) throws JsonRpcStatusException, IOException {
        String hexEncoded = send("getrawtransaction", txid);
        byte[] raw = HexUtil.hexStringToByteArray(hexEncoded);
        return new Transaction(netParams, raw);
    }

    /**
     * Get a "raw" transaction as JSON (which we map to a RawTransactionInfo POJO)
     * @param txid Transaction ID/hash
     * @return RawTransactionInfo POJO
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public RawTransactionInfo getRawTransactionInfo(Sha256Hash txid) throws JsonRpcStatusException, IOException {
        return send("getrawtransaction", RawTransactionInfo.class, txid, 1);
    }

    /**
     * Submit a raw transaction to local node and network
     *
     * @since Bitcoin Core 0.19
     * @param tx The raw transaction
     * @param maxFeeRate  Reject transactions whose fee rate is higher than this value, expressed in BTC/kB.
     *                    Set to 0 to accept any fee rate.
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Sha256Hash sendRawTransaction(Transaction tx, Coin maxFeeRate) throws JsonRpcStatusException, IOException {
        return send("sendrawtransaction", Sha256Hash.class, tx, maxFeeRate);
    }

    /**
     * Submit a raw transaction to local node and network
     *
     * @since Bitcoin Core 0.19
     * @param hexTx The raw transaction as a hex-encoded string
     * @param maxFeeRate  Reject transactions whose fee rate is higher than this value, expressed in BTC/kB.
     *                    Set to 0 to accept any fee rate.
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Sha256Hash sendRawTransaction(String hexTx, Coin maxFeeRate) throws JsonRpcStatusException, IOException {
        return send("sendrawtransaction", Sha256Hash.class, hexTx, maxFeeRate);
    }

    /**
     * Submit a raw transaction to local node and network using server's default `maxFeeRate`
     *
     * @param tx The raw transaction
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Sha256Hash sendRawTransaction(Transaction tx) throws JsonRpcStatusException, IOException {
        return sendRawTransaction(tx, (Coin) null);
    }

    /**
     * Submit a raw transaction to local node and network using server's default `maxFeeRate`
     *
     * @since Bitcoin Core 0.19
     * @param hexTx The raw transaction as a hex-encoded string
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Sha256Hash sendRawTransaction(String hexTx) throws JsonRpcStatusException, IOException {
        return sendRawTransaction(hexTx, (Coin) null);
    }

    /**
     * Submit a raw transaction to local node and network
     *
     * @param tx The raw transaction
     * @param allowHighFees deprecated boolean parameter
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     * @deprecated In Bitcoin Core 0.19 and later, use {@link BitcoinClient#sendRawTransaction(Transaction, Coin)}
     */
    @Deprecated
    public Sha256Hash sendRawTransaction(Transaction tx, Boolean allowHighFees) throws JsonRpcStatusException, IOException {
        return send("sendrawtransaction", Sha256Hash.class, tx, allowHighFees);
    }

    /**
     * Submit a raw transaction to local node and network
     *
     * @param hexTx The raw transaction as a hex-encoded string
     * @param allowHighFees deprecated boolean parameter
     * @return SHA256 Transaction ID
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     * @deprecated In Bitcoin Core 0.19 and later, use {@link BitcoinClient#sendRawTransaction(Transaction, Coin)}
     */
    @Deprecated
    public Sha256Hash sendRawTransaction(String hexTx, Boolean allowHighFees) throws JsonRpcStatusException, IOException {
        return send("sendrawtransaction", Sha256Hash.class, hexTx, allowHighFees);
    }

    public AddressInfo getAddressInfo(Address address) throws JsonRpcStatusException, IOException {
        return send("getaddressinfo", AddressInfo.class, address);
    }

    public Coin getReceivedByAddress(Address address) throws JsonRpcStatusException, IOException {
        return getReceivedByAddress(address, 1);   // Default to 1 or more confirmations
    }

    /**
     * get total amount received by an address.
     *
     * @param address Address to query
     * @param minConf minimum number of confirmations
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Coin getReceivedByAddress(Address address, Integer minConf) throws JsonRpcStatusException, IOException {
        return send("getreceivedbyaddress", Coin.class, address, minConf);
    }

    public List<ReceivedByAddressInfo> listReceivedByAddress(Integer minConf, Boolean includeEmpty)
            throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, ReceivedByAddressInfo.class);
        return send("listreceivedbyaddress", resultType, minConf, includeEmpty);
    }

    /**
     * Returns a list of unspent transaction outputs with at least one confirmation.
     *
     * @return The unspent transaction outputs
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<UnspentOutput> listUnspent() throws JsonRpcStatusException, IOException {
        return listUnspent(null, null, null, null);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @return The unspent transaction outputs
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf)
            throws JsonRpcStatusException, IOException {
        return listUnspent(minConf, maxConf, null, null);
    }

    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf, Address address)
            throws JsonRpcStatusException, IOException {
        return listUnspent(minConf, maxConf, List.of(address), true);
    }

    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf, List<Address> filter)
            throws JsonRpcStatusException, IOException {
        return listUnspent(minConf, maxConf, filter, true);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations, filtered by a list of addresses.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @param filter  Include only transaction outputs to the specified addresses
     * @param includeUnsafe optional
     * @return The unspent transaction outputs
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf, List<Address> filter, Boolean includeUnsafe)
            throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, UnspentOutput.class);
        return send("listunspent", resultType, minConf, maxConf, filter, includeUnsafe);
    }

    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid The transaction hash
     * @param vout The transaction output index
     * @return Details about an unspent output or nothing, if the output was already spent
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public TxOutInfo getTxOut(Sha256Hash txid, Integer vout) throws JsonRpcStatusException, IOException {
        return getTxOut(txid, vout, null);
    }


    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid              The transaction hash
     * @param vout              The transaction output index
     * @param includeMemoryPool Whether to included the memory pool
     * @return Details about an unspent output or nothing, if the output was already spent
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public TxOutInfo getTxOut(Sha256Hash txid, Integer vout, Boolean includeMemoryPool)
            throws JsonRpcStatusException, IOException {
        return send("gettxout", TxOutInfo.class, txid, vout, includeMemoryPool);
    }

    /**
     * Returns statistics about the unspent transaction output set.
     * Note this call may take some time.
     *
     * @return statistics about the unspent transaction output set
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public TxOutSetInfo getTxOutSetInfo() throws JsonRpcStatusException, IOException {
        return send("gettxoutsetinfo", TxOutSetInfo.class);
    }

    /**
     * Get the balance for a the default Bitcoin "account"
     *
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Coin getBalance() throws JsonRpcStatusException, IOException {
        return getBalance(null, null);
    }

    /**
     * Get the balance for a Bitcoin "account"
     *
     * @param account A Bitcoin "account". (Be wary of using this feature.)
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Coin getBalance(String account) throws JsonRpcStatusException, IOException {
        return getBalance(account, null);
    }

    /**
     * Get the balance for a Bitcoin "account"
     *
     * @param account A Bitcoin "account". (Be wary of using this feature.)
     * @param minConf minimum number of confirmations
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Coin getBalance(String account, Integer minConf) throws JsonRpcStatusException, IOException {
        return send("getbalance", Coin.class, account, minConf);
    }

    public Sha256Hash sendToAddress(Address address, Coin amount) throws JsonRpcStatusException, IOException {
        return sendToAddress(address, amount, null, null);
    }

    public Sha256Hash sendToAddress(Address address, Coin amount, String comment, String commentTo)
            throws JsonRpcStatusException, IOException {
        return send("sendtoaddress", Sha256Hash.class, address, amount, comment, commentTo);
    }

    public Sha256Hash sendFrom(String account, Address address, Coin amount)
            throws JsonRpcStatusException, IOException {
        return send("sendfrom", Sha256Hash.class, account, address, amount);
    }

    public Sha256Hash sendMany(String account, Map<Address, Coin> amounts) throws JsonRpcStatusException, IOException {
        return send("sendmany", Sha256Hash.class, account, amounts);
    }

    /**
     * Set the transaction fee per kB.
     *
     * @param amount The transaction fee in BTC/kB rounded to the nearest 0.00000001.
     * @return True if successful
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Boolean setTxFee(Coin amount) throws JsonRpcStatusException, IOException {
        return send("settxfee", amount);
    }

    public WalletTransactionInfo getTransaction(Sha256Hash txid) throws JsonRpcStatusException, IOException {
        return getTransaction(txid, null, null);
    }

    public WalletTransactionInfo getTransaction(Sha256Hash txid, Boolean includeWatchOnly, Boolean verbose) throws JsonRpcStatusException, IOException {
        return send("gettransaction", WalletTransactionInfo.class, txid, includeWatchOnly, verbose);
    }

    /**
     * List the default number (10) of wallet transactions.
     * @return A list of wallet transactions
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<BitcoinTransactionInfo> listTransactions() throws JsonRpcStatusException, IOException {
        return listTransactions(null, null, null, null);
    }

    /**
     * List wallet transactions.
     * @param label Return transactions matching this address label, use "*" to return all transactions. {@code null} will use the server default ({@code "*"})
     * @param count Maximum number of transactions to return. {@code null} will use the sever default number (10)
     * @return A list of wallet transactions
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<BitcoinTransactionInfo> listTransactions(String label, Integer count) throws JsonRpcStatusException, IOException {
        return listTransactions(label, count, null, null);
    }
    /**
     * List wallet transactions
     * @param label Return transactions matching this address label, use "*" to return all transactions. {@code null} will use the server default ({@code "*"})
     * @param count Maximum number of transactions to return. {@code null} will use the sever default number (10)
     * @param skip Number of transactions to skip. {@code null} will use the server default (0)
     * @param includeWatchOnly Include transactions to/from watch-only addresses. {@code null} will use the server default, which is  ({@code true}) for watch-only wallets, {@code false} otherwise.
     * @return A list of wallet transactions
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<BitcoinTransactionInfo> listTransactions(String label, Integer count, Integer skip, Boolean includeWatchOnly) throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, BitcoinTransactionInfo.class);
        return send("listtransactions", resultType, label, count, skip, includeWatchOnly);
    }

    /**
     * The getblockchaininfo RPC provides information about the current state of the block chain.
     *
     * @return An object containing information about the current state of the block chain.
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public BlockChainInfo getBlockChainInfo() throws JsonRpcStatusException, IOException {
        return send("getblockchaininfo", BlockChainInfo.class);
    }

    /**
     * The getnetworkinfo RPC returns information about the node's connection to the network.
     *
     * @return information about the node's connection to the network
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public NetworkInfo getNetworkInfo() throws JsonRpcStatusException, IOException  {
        return send("getnetworkinfo", NetworkInfo.class);
    }

    /**
     * The getzmqnotifications RPC returns information about which configured ZMQ notifications are enabled
     * and on which ports.
     * 
     * @return A List of ZMQ Notification info records
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<ZmqNotification> getZmqNotifications() throws JsonRpcStatusException, IOException  {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, ZmqNotification.class);
        return send("getzmqnotifications", resultType);
    }

    /**
     * Returns list of related addresses
     * Also useful for finding all change addresses in the wallet
     * @return a lost of address groupings
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<List<AddressGroupingItem>>  listAddressGroupings() throws JsonRpcStatusException, IOException {
        // TODO: I'm not sure how to make Jackson mapping work automatically here.
        List<List<List<Object>>> raw = send("listaddressgroupings");
        List<List<AddressGroupingItem>> result = new ArrayList<>();
        for (List<List<Object>> rawGrouping : raw) {
            List<AddressGroupingItem> grouping = new ArrayList<>();
            for (List<Object> addressItem : rawGrouping) {
                AddressGroupingItem item = new AddressGroupingItem(addressItem, getNetParams());
                grouping.add(item);
            }
            result.add(grouping);
        }
        return result;
    }

    /**
     * Returns a human-readable list of available commands.
     * <p>
     * Bitcoin Core 0.10+ returns a categorized list of commands including blank lines
     * and header lines.
     *
     * @return The list of commands as string
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public String help() throws JsonRpcStatusException, IOException {
        return help(null);
    }

    /**
     * Returns a human-readable list of available commands.
     *
     * @return The response as a stream of one {@link String} for each line
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Stream<String> helpAsStream() throws JsonRpcStatusException, IOException {
        return lines(help());
    }

    /**
     * Returns a human-readable list of available commands.
     *
     * @return The response as a list one {@link String} for each line
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<String> helpAsLines() throws JsonRpcStatusException, IOException {
        return helpAsStream().collect(Collectors.toList());
    }

    /**
     * Returns a human-readable list of available commands.
     *
     * @return The response as a list one {@link MethodHelpEntry} for each method
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<MethodHelpEntry> helpAsMethodEntries() throws JsonRpcStatusException, IOException {
        return helpAsStream()
                .filter(this::isMethodEntry)
                .map(MethodHelpEntry::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns helpful information for a specific command.
     *
     * @param command The name of the command to get help for
     * @return The help text
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public String help(String command) throws JsonRpcStatusException, IOException {
        return send("help", command);
    }

    /**
     * Returns a list of available commands.
     *
     * Commands which are unavailable will not be listed, such as wallet RPCs, if wallet support is disabled.
     *
     * @return The list of commands
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public List<String> getCommands() throws JsonRpcStatusException, IOException {
        return helpAsMethodEntries().stream()
                .map(MethodHelpEntry::getMethodName)
                .collect(Collectors.toList());
    }

    private Stream<String> lines(String string) {
        // In JDK 11 this can be replaced by String::lines
        return Stream.of(string.split("\n"));
    }

    private boolean isMethodEntry(String line) {
        // Filter out blank and header lines
        return !line.isEmpty() && !line.matches("== (.+) ==");
    }

    /**
     * Checks whether a command exists.
     *
     * This is done indirectly, by using {help(String) help} to get information about the command, and if information
     * about the command is available, then the command exists. The absence of information does not necessarily imply
     * the non-existence of a command.
     *
     * @param command The name of the command to check
     * @return True if the command exists
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public Boolean commandExists(String command) throws JsonRpcStatusException, IOException {
        return !help(command).contains("help: unknown command");
    }

    /**
     * Permanently marks a block as invalid, as if it violated a consensus rule.
     *
     * @param hash The block hash
     * @since Bitcoin Core 0.10
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public void invalidateBlock(Sha256Hash hash) throws JsonRpcStatusException, IOException {
        send("invalidateblock", hash);
    }

    /**
     * Removes invalidity status of a block and its descendants, reconsider them for activation.
     * <p>
     * This can be used to undo the effects of {link invalidateBlock(Sha256Hash) invalidateBlock}.
     *
     * @param hash The hash of the block to reconsider
     * @since Bitcoin Core 0.10
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public void reconsiderBlock(Sha256Hash hash) throws JsonRpcStatusException, IOException {
        send("reconsiderblock", hash);
    }

    /**
     * Return information about all known tips in the block tree, including the main chain as well as orphaned branches.
     *
     * @return A list of chain tip information
     * @since Bitcoin Core 0.10
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    @Override
    public List<ChainTip> getChainTips() throws JsonRpcStatusException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, ChainTip.class);
        return send("getchaintips",resultType);
    }

    /**
     * Attempt to add or remove a node from the addnode list, or to try a connection to a node once.
     *
     * @param node node to add as a string in the form of {@code IP_address:port}
     * @param command `add`, `remove`, or `onetry`
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public void addNode(String node, String command) throws JsonRpcStatusException, IOException {
        send("addnode", node, command);
    }

    /**
     * Return information about the given added node
     *
     * @param details `true` to return detailed information
     * @param node the node to provide information about
     * @return A Jackson JsonNode object (until we define a POJO)
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public JsonNode getAddedNodeInfo(boolean details, String node) throws JsonRpcStatusException, IOException  {
        return send("getaddednodeinfo", JsonNode.class, details, node);
    }

    /**
     * Return information about all added nodes
     *
     * @param details `true` to return detailed information
     * @return A Jackson JsonNode object (until we define a POJO)
     * @throws JsonRpcStatusException JSON RPC status exception
     * @throws IOException network error
     */
    public JsonNode getAddedNodeInfo(boolean details) throws JsonRpcStatusException, IOException  {
        return getAddedNodeInfo(details, null);
    }

    // Bitcore/Omni transaction for getting non-wallet address balances
    public AddressBalanceInfo getAddressBalance(Address address) throws JsonRpcException, IOException {
        return send("getaddressbalance", AddressBalanceInfo.class, address);
    }

    // Bitcore/Omni transaction for getting non-wallet address balances
    public AddressBalanceInfo getAddressBalance(List<Address> addressList) throws JsonRpcException, IOException {
        return send("getaddressbalance", AddressBalanceInfo.class, new AddressRequest(addressList));
    }

    // Bitcore/Omni transaction for getting non-wallet address UTXO Info
    public List<AddressUtxoInfo>  getAddressUtxos(Address address) throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, AddressUtxoInfo.class);
        return send("getaddressutxos", resultType, address);
    }

    // Bitcore/Omni transaction for getting non-wallet address UTXO Info
    public List<AddressUtxoInfo> getAddressUtxos(List<Address> addressList) throws JsonRpcException, IOException {
        JavaType resultType = mapper.getTypeFactory().constructCollectionType(List.class, AddressUtxoInfo.class);
        return send("getaddressutxos", resultType, new AddressRequest(addressList));
    }

}