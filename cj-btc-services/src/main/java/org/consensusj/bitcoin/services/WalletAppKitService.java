package org.consensusj.bitcoin.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.DefaultAddressParser;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.conversion.RpcServerModule;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;
import org.consensusj.bitcoin.json.rpc.BitcoinJsonRpc;
import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.store.BlockStoreException;
import org.consensusj.bitcoinj.signing.RawTransactionSigningRequest;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionOutputData;
import org.consensusj.bitcoinj.signing.TransactionOutputDataScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implement a subset of Bitcoin JSON RPC using a WalletAppKit
 */
@Named
public class WalletAppKitService implements BitcoinJsonRpc, Closeable {
    private static final Logger log = LoggerFactory.getLogger(WalletAppKitService.class);
    // P2P user-agent string
    private static final String userAgentName = "WalletAppKitService";
    // P2P user-agent version
    private static final String appVersion = "0.1";
    // server version, placeholder for now (returned in `getnetworkinfo`)
    private static final int version = 1;
    private static final String helpString = """
== Blockchain ==
getbestblockhash
getblock hash verbosity(0,1,2)
getblockchaininfo
getblockcount
getblockhash height [TBD]
getblockheader hash verbose(boolean)

== Control ==
help
stop

== Network ==
getconnectioncount
getnetworkinfo

== Rawtransactions ==
createrawtransaction [{"txid":"hex","vout":n},...] [{"address":amount},...]
sendrawtransaction hex

== Wallet ==
getbalance
getnewaddress
listunspent minConf maxConf addresses include-unsafe
sendtoaddress address amount
signrawtransactionwithwallet hex
""";

    protected final HexFormat hexFormat = HexFormat.of();

    protected final BitcoinNetwork network;
    protected final WalletAppKit kit;
    protected final ObjectMapper mapper;

    /* Dummy fields for JSON RPC responses TODO: Implement them */
    private int timeOffset = 0;
    private BigDecimal difficulty = new BigDecimal(0);
    private BigDecimal verificationProgress = new BigDecimal(0);
    private byte[] chainWork = new byte[]{0x00, 0x00};

    private static final AddressParser parser = new DefaultAddressParser();
    private WalletSigningService signingService;

    /**
     * Create an instance in a temporary directory. Useful for testing.
     * @param network network to operate on
     * @param scriptType script type for wallet
     * @param walletBaseName base name for the two wallet files
     * @return an un-started instance (use {@link #start()} to start it)
     */
    public static WalletAppKitService createTemporary(BitcoinNetwork network, ScriptType scriptType, String walletBaseName) {
        WalletAppKit walletAppKit = createTemporaryWallet(network, scriptType, walletBaseName);
        return new WalletAppKitService(walletAppKit);
    }

    public static WalletAppKit createTemporaryWallet(BitcoinNetwork network, ScriptType scriptType, String walletBaseName) {
        Context.propagate(new Context());
        String filePrefix = walletBaseName + "-" + network;
        File dataDirectory = null;
        try {
            dataDirectory = Files.createTempDirectory(walletBaseName).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Returning WalletAppKit bean, wallet directory: {}, prefix: {}", dataDirectory.getAbsolutePath(), filePrefix);
        return new WalletAppKit(network, scriptType, KeyChainGroupStructure.BIP43, dataDirectory, filePrefix);
    }

    /**
     * Construct from an (un-started) {@link WalletAppKit} instance.
     * @param walletAppKit instance
     */
    @Inject
    public WalletAppKitService(WalletAppKit walletAppKit) {
        kit = walletAppKit;
        network = kit.network();
        mapper = new ObjectMapper();
        mapper.registerModule(new RpcServerModule());
    }

    @PostConstruct
    public void start() {
        log.info("WalletAppKitService.start()");
        kit.setUserAgent(userAgentName, appVersion);
        kit.setBlockingStartup(false);  // `false` means startup is complete when network activity begins
        kit.startAsync();
        kit.awaitRunning();
        signingService = new WalletSigningService(kit.wallet());
    }

    public Network network() {
        return this.network;
    }

    public PeerGroup getPeerGroup() {
        kit.awaitRunning();
        return kit.peerGroup();
    }

    @Override
    public CompletableFuture<String> help() {
        return result(helpString);
    }

    @Override
    public CompletableFuture<String> stop() {
        log.info("stop command received, ignoring...");
        return result("stop command ignored.");
    }

    @Override
    public CompletableFuture<Integer> getblockcount() {
        log.info("getblockcount");
        if(!kit.isRunning()) {
            String state = kit.state().toString();
            log.warn("WalletAppKit not running, state: {}", state);
            return exception(new RuntimeException("WalletAppKit not running, state: " + state));
        }
        return result(kit.chain().getChainHead().getHeight());
    }

    @Override
    public CompletableFuture<Sha256Hash> getbestblockhash() {
        log.info("getbestblockhash");
        if(!kit.isRunning()) {
            log.warn("kit not running, returning null");
            return null;
        }
        return result(kit.chain().getChainHead().getHeader().getHash());
    }

    @Override
    public CompletableFuture<JsonNode> getblockheader(String blockHashString, Boolean verbose) {
        return getblockheader2(Sha256Hash.wrap(blockHashString), verbose);
    }

    public CompletableFuture<JsonNode> getblockheader2(Sha256Hash blockHash, Boolean verbose) {
        if (verbose == null || verbose) {
            return getBlockInfo(blockHash, BlockInfo.IncludeTxFlag.NO)
                    .thenApply(mapper::valueToTree);
        } else {
            return getBlockBytes(blockHash)
                    .thenApply(HexUtil::bytesToHexString)
                    .thenApply(mapper::valueToTree);
        }
    }

    /**
     * Partial implementation of `getblock`
     *
     * In the case where verbosity = 0, a block is returned without transactions (which is incorrect)
     * For verbosity = 1, we return a BlockInfo, but some fields may be missing or incorrect, including a list of TXIDs
     * For verbosity = 2, we are currently throwing an exception.
     * 
     * @param blockHashString The hash of the block we are to return (as hex)
     * @param verbosity Specifies the format to return (currently only `1` halfway works)
     * @return Either a `Block` or `BlockInfo` depending upon verbosity.
     */
    @Override
    public CompletableFuture<JsonNode> getblock(String blockHashString, Integer verbosity) {
        return getblock2(Sha256Hash.wrap(blockHashString), verbosity);
    }

    public CompletableFuture<JsonNode> getblock2(Sha256Hash blockHash, Integer verbosity) {
        int verbosityInt = verbosity != null ? verbosity : 1;
        return (switch (verbosityInt) {
            case 0 -> getBlockBytes(blockHash).thenApply(HexUtil::bytesToHexString);
            case 1 -> getBlockInfo(blockHash, BlockInfo.IncludeTxFlag.IDONLY);
            case 2 -> getBlockInfo(blockHash, BlockInfo.IncludeTxFlag.YES);
            default -> exception(new IllegalArgumentException("Unknown verbosity parameter"));
        }).thenApply(mapper::valueToTree);
    }

    /**
     * Placeholder -- currently throws `UnsupportedOperationException`
     * 
     * @param blockNumber The block height of the desired block
     * @return The Sha256Hash of the specified block
     */
    @Override
    public CompletableFuture<Sha256Hash> getblockhash(Integer blockNumber) {
        // TODO: Extend Blockchain/Blockstore so it can do this
        return exception(new UnsupportedOperationException("Unimplemented RPC method"));
    }

    @Override
    public CompletableFuture<Integer> getconnectioncount() {
        if(!kit.isRunning()) {
            return exception(new RuntimeException("Kit not running"));
        }
        try {
            return result(kit.peerGroup().numConnectedPeers());
        } catch (IllegalStateException ex) {
            return exception(ex);
        }
    }

    @Override
    public CompletableFuture<BlockChainInfo> getblockchaininfo() {
        return result(new BlockChainInfo(chainName(network),        // Chain ID
                kit.chain().getChainHead().getHeight(),             // Block processed (same as headers for SPV)
                kit.chain().getChainHead().getHeight(),             // Headers validated
                kit.chain().getChainHead().getHeader().getHash(),   // Best block hash
                difficulty,
                verificationProgress,
                chainWork));
    }

    /**
     * Map {@link BitcoinNetwork} to a chain-id string.
     * Bitcoin Core returns strings that differ from {@link BitcoinNetwork#toString()}.
     * @param network bitcoinj enum type
     * @return Bitcoin Core-compatible <q>chain</q> string
     */
    private String chainName(BitcoinNetwork network) {
        return switch(network) {
            case MAINNET -> "main";
            case TESTNET -> "test";
            case SIGNET, REGTEST -> network.toString();
        };
    }

    @Override
    public CompletableFuture<NetworkInfo> getnetworkinfo() {
        byte[] localServices = {};
        Object[] network = {};
        Object[] address = {};
        return result(new NetworkInfo(version,
                "",
                NetworkParameters.ProtocolVersion.CURRENT.getBitcoinProtocolVersion(),
                timeOffset,
                getconnectioncount().join(),
                "proxy",
                (int) Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.getValue(),  // relayFee
                localServices,
                network,
                address));
    }

    record outpoint(Sha256Hash txId, int vout) {
        public outpoint(String idString, int vout) {
            this(Sha256Hash.wrap(idString), vout);
        }
        public outpoint(Map<String, Object> json) {
            this((String) json.get("txid"), ((Number) json.get("vout")).intValue());
        }

    }
    record output(Address address, Coin amount) {
        public output(String addString, String amountString) {
            this(parser.parseAddressAnyNetwork(addString),  Coin.parseCoin(amountString));
        }
        Script script() {
            return ScriptBuilder.createP2PKHOutputScript(this.address().getHash());
        }
    }

    @Override
    public CompletableFuture<String> createrawtransaction(List<Map<String, Object>> inputs, List<Map<String, String>> outputs) {
        try {
            List<TransactionInputData> ins = inputs.stream()
                    .map(outpoint::new)
                    .flatMap(op -> signingService.findUnspentOutput(op.txId, op.vout).stream())
                    .map(TransactionInputData::fromTxOut)
                    .toList();
            List<? extends TransactionOutputData> outs = outputs.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .map(entry -> new output(entry.getKey(), entry.getValue()))
                    .map(o -> new TransactionOutputDataScript(o.amount(), o.script()))
                    .toList();
            // TODO: Add a change output?
            SigningRequest sr = SigningRequest.of(network, ins, (List<TransactionOutputData>) outs);
            Transaction rawTx = sr.toUnsignedTransaction();
            return CompletableFuture.completedFuture(rawTx.toHexString());
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(t);
        }
    }

    @Override
    public CompletableFuture<Address> getnewaddress() {
        DeterministicKey key = kit.wallet().getActiveKeyChain().getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS);
        Address address = key.toAddress(ScriptType.P2PKH, network);
        log.warn("address {} key path: {}", address, key.getPathAsString());
        return CompletableFuture.completedFuture(address);
    }

    @Override
    public CompletableFuture<Coin> getbalance() {
        return CompletableFuture.completedFuture(kit.wallet().getBalance());
    }

    @Override
    public CompletableFuture<List<UnspentOutput>> listunspent(Integer minConf, Integer maxConf, List<String> addresses, Boolean includeUnsafe) {
        List<Address> addressList = addresses != null
                ? addresses.stream().map(parser::parseAddressAnyNetwork).toList()
                : List.of();
        List<TransactionOutput> outs = signingService.findUnspentOutputs(
                                            minConf != null ? minConf : DEFAULT_MIN_CONF,
                                            maxConf != null ? maxConf : DEFAULT_MAX_CONF,
                                            addressList);
        List<UnspentOutput> unspents = outs.stream()
                .map(out -> new UnspentOutput(
                        out.getParentTransactionHash(),
                        out.getIndex(),
                        null, // Address (get from script?)
                        "", // label
                        hexFormat.formatHex(out.getScriptPubKey().getProgram()), // scriptPubKey
                        out.getValue(),     // amount
                        out.getParentTransactionDepthInBlocks(),  // confirmations
                        null,   // redeemScript
                        null,   // witnessScript
                        true,   // spendable
                        true,   // solvable
                        "description",  // description
                        true)   // safe
                )
                .toList();
        return CompletableFuture.completedFuture(unspents);
    }

    /**
     * Send coins to an address
     * @param toAddressString Address to send coins to
     * @param amountDouble Amount to send
     * @return future that completes with a transaction hash/id when the tx is successfully relayed
     */
    @Override
    public CompletableFuture<Sha256Hash> sendtoaddress(String toAddressString, Double amountDouble) {
        log.info("sendtoaddress {}, {}", toAddressString, amountDouble);
        Address toAddress = parser.parseAddress(toAddressString, network);
        Coin amount = Coin.ofBtc(BigDecimal.valueOf(amountDouble));
        Transaction signedTx;
        try {
            signedTx = signingService.signSendToAddress(toAddress, amount).join();
        } catch (IOException | InsufficientMoneyException e) {
            return CompletableFuture.failedFuture(e);
        }
        return sendTransaction(signedTx)
                .thenCompose(tb1 -> tb1.awaitRelayed()
                        .thenApply(tb2 -> tb2.transaction()
                                .getTxId()
                        )
                );
    }

    @Override
    public CompletableFuture<Sha256Hash> sendrawtransaction(String hex) {
        log.info("received raw tx: {}", hex);
        ByteBuffer raw = ByteBuffer.wrap(hexFormat.parseHex(hex));
        Transaction signedTx;
        try {
            signedTx = new Transaction(NetworkParameters.of(network), raw);
        } catch (ProtocolException pe) {
            return CompletableFuture.failedFuture(new RuntimeException("Invalid raw (hex) transaction", pe));
        }
        log.info("received raw tx: {}", signedTx);
        try {
            signedTx.verify();
        } catch (VerificationException ve) {
            // TODO: More validation?
            return CompletableFuture.failedFuture(ve);
        }
        return sendTransaction(signedTx)
                .whenComplete((bcast, error) -> {
                    if (error != null) {
                        log.info("Broadcast {}, done: {}", bcast.transaction().getTxId(), bcast.awaitSent().isDone());
                    } else {
                        log.error("", error);
                    }
                })
                .thenCompose(tb1 -> tb1.awaitRelayed()
                        .thenApply(tb2 -> tb2.transaction()
                                .getTxId()
                        )
                );
    }

    /**
     * @param hex hex-encoded unsigned raw transaction
     * @return signed raw transaction
     */
    @Override
    public CompletableFuture<SignedRawTransaction> signrawtransactionwithwallet(String hex) {
        ByteBuffer raw = ByteBuffer.wrap(hexFormat.parseHex(hex));

        RawTransactionSigningRequest signingRequest;
        try {
            Transaction unsignedTx = new Transaction(NetworkParameters.of(network), raw);
            log.info("received tx: {}", unsignedTx);
            signingRequest = RawTransactionSigningRequest.ofTransaction(network, unsignedTx);
        } catch (ProtocolException e) {
            return CompletableFuture.failedFuture(new RuntimeException("Invalid raw (hex) transaction", e));
        }

        return signingService.signTransaction(signingRequest)
                .thenApply(SignedRawTransaction::of);
    }

    private CompletableFuture<TransactionBroadcast> sendTransaction(Transaction tx) {
        try {
            log.info("committing tx to wallet: {}", tx);
            kit.wallet().commitTx(tx);
            log.info("broadcasting tx: {}", tx);
            return kit.peerGroup().broadcastTransaction(tx).awaitSent();
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(t);
        }
    }

    private CompletableFuture<byte[]> getBlockBytes(Sha256Hash blockHash) {
        // TODO: This block should have transactions
        StoredBlock storedBlock;
        try {
            storedBlock = getStoredBlockByHash(kit.chain(), blockHash);
        } catch (BlockStoreException e) {
            return exception(e);
        }
        byte[] data = storedBlock.getHeader().bitcoinSerialize();
        return result(data);
    }

    public CompletableFuture<BlockInfo> getBlockInfo(Sha256Hash blockHash, BlockInfo.IncludeTxFlag includeTx) {
        BlockInfo blockInfo;
        try {
            blockInfo = getBlockInfoByHash(kit.chain(), blockHash, includeTx);
            log.info("blockinfo: {}, {}", blockInfo.hash, blockInfo.height);
        } catch (BlockStoreException e) {
            return exception(e);
        }
        return result(blockInfo);
    }

    protected <T> CompletableFuture<T> result(T result) {
        return CompletableFuture.completedFuture(result);
    }

    private <T> CompletableFuture<T> exception(Throwable exception) {
        return CompletableFuture.failedFuture(exception);
    }

    private static StoredBlock getStoredBlockByHash(AbstractBlockChain blockChain, Sha256Hash blockHash) throws BlockStoreException {
        return blockChain.getBlockStore().get(blockHash);
    }

    /**
     * Get a BlockInfo for the specified hash
     *
     * @param blockChain The blockchain object to pull the data from
     * @param blockHash The hash of the desired block
     * @param includeTx whether to include transactions (currently must be false)
     * @return block information (currently incomplete and untested)
     * @throws BlockStoreException Something went wrong
     */
    private static BlockInfo getBlockInfoByHash(AbstractBlockChain blockChain, Sha256Hash blockHash, BlockInfo.IncludeTxFlag includeTx) throws BlockStoreException {
        if (includeTx == BlockInfo.IncludeTxFlag.YES) {
            throw new IllegalArgumentException("Including transactions not supported yet");
        }
        StoredBlock block = getStoredBlockByHash(blockChain, blockHash);
        Block header = block.getHeader();
        int blockHeight = block.getHeight();
        int confirmations = blockChain.getBestChainHeight() - blockHeight;
        log.trace("building BlockInfo for hash: {} height: {}", blockHash, blockHeight);
        return new BlockInfo(header.getHash(),
                confirmations,
                header.getMessageSize(),
                blockHeight,
                (int) header.getVersion(),
                header.getMerkleRoot(),
                -1,     // Unknown number of Transactions
                (includeTx == BlockInfo.IncludeTxFlag.IDONLY) ? hashListFromTxList(header.getTransactions()) : null,
                (int) header.time().getEpochSecond(),
                header.getNonce(),
                null, // TODO: Return "bits" here
                new BigDecimal(header.getDifficultyTargetAsInteger()),  // TODO: Verify this is correct
                block.getChainWork().toString(),
                block.getPrev(blockChain.getBlockStore()).getHeader().getHash(),
                null);  // TODO: Extend BlockStore to make this information retrievable
    }

    private static BlockInfo.Sha256HashList hashListFromTxList(List<Transaction> txList) {
        if (txList == null) {
            return null;
        } else {
            List<Sha256Hash> list = txList.stream()
                    .map(Transaction::getTxId)
                    .collect(Collectors.toList());
            return new BlockInfo.Sha256HashList(list);
        }
    }

    @Override
    @PreDestroy
    public void close() {
        log.info("Closing WalletAppKit...");
        kit.close();
        kit.awaitTerminated();  // This should be done by WalletAppKit itself, see https://github.com/bitcoinj/bitcoinj/pull/3028
        log.info("WalletAppKit terminated");
    }
}
