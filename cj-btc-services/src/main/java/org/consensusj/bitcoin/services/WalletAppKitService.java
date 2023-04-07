package org.consensusj.bitcoin.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.json.pojo.ServerInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implement a subset of Bitcoin JSON RPC using a WalletAppKit
 */
@Named
public class WalletAppKitService implements BitcoinJsonRpc {
    private static final Logger log = LoggerFactory.getLogger(WalletAppKitService.class);
    private static final String userAgentName = "PeerList";
    private static final String appVersion = "0.1";
    private static final int version = 1;
    private static final int protocolVersion = 1;
    private static final int walletVersion = 0;
    private static final String helpString = """
            getbestblockhash
            getblock hash verbosity(0,1,2)
            getblockchaininfo
            getblockcount
            getblockhash height (not implemented)
            getblockheader hash verbose(boolean)
            getconnectioncount
            getinfo (deprecated)
            getnetworkinfo
            help
            stop
    """;

    protected final BitcoinNetwork network;
    protected final WalletAppKit kit;
    protected final ObjectMapper mapper;

    /* Dummy fields for JSON RPC responses TODO: Implement them */
    private int timeOffset = 0;
    private BigDecimal difficulty = new BigDecimal(0);
    private BigDecimal verificationProgress = new BigDecimal(0);
    private byte[] chainWork = new byte[]{0x00, 0x00};

    /**
     * Construct from an (unstarted) {@link WalletAppKit} instance.
     * @param walletAppKit instance
     */
    @Inject
    public WalletAppKitService(WalletAppKit walletAppKit) {
        kit = walletAppKit;
        network = kit.network();
        mapper = new ObjectMapper();
    }

    @PostConstruct
    public void start() {
        log.info("WalletAppKitService.start()");
        kit.setUserAgent(userAgentName, appVersion);
        kit.setBlockingStartup(false);  // `false` means startup is complete when network activity begins
        kit.startAsync();
        kit.awaitRunning();
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
     * Placeholder -- currently throws `IllegalArgumentException`
     * 
     * @param blockNumber The block height of the desired block
     * @return The Sha256Hash of the specified block
     */
    @Override
    public CompletableFuture<Sha256Hash> getblockhash(Integer blockNumber) {
        // TODO: Extend Blockchain/Blockstore so it can do this
        return exception(new IllegalArgumentException("Unsupported RPC method"));
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

    @Deprecated
    public CompletableFuture<ServerInfo> getinfo() {
        // Dummy up a response for now.
        // Since ServerInfo is immutable, we have to build it entirely with the constructor.
        Coin balance = Coin.valueOf(0);
        boolean testNet = !network.id().equals(BitcoinNetwork.ID_MAINNET);
        int keyPoolOldest = 0;
        int keyPoolSize = 0;
        return result(new ServerInfo(
                version,
                protocolVersion,
                walletVersion,
                balance,
                getblockcount().join(),
                timeOffset,
                getconnectioncount().join(),
                "proxy",
                difficulty,
                testNet,
                keyPoolOldest,
                keyPoolSize,
                Transaction.REFERENCE_DEFAULT_MIN_TX_FEE,
                Transaction.REFERENCE_DEFAULT_MIN_TX_FEE, // relayfee
                "no errors"                        // errors
        ));
    }

    @Override
    public CompletableFuture<BlockChainInfo> getblockchaininfo() {
        return result(new BlockChainInfo("main",                     // Chain ID
                kit.chain().getChainHead().getHeight(),             // Block processed
                kit.chain().getChainHead().getHeight(),             // Headers validated
                kit.chain().getChainHead().getHeader().getHash(),   // Best block hash
                difficulty,
                verificationProgress,
                chainWork));
    }

    @Override
    public CompletableFuture<NetworkInfo> getnetworkinfo() {
        byte[] localServices = {};
        Object[] network = {};
        Object[] address = {};
        return result(new NetworkInfo(version,
                "",
                protocolVersion,
                timeOffset,
                getconnectioncount().join(),
                "proxy",
                0,
                localServices,
                network,
                address));
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
                (int) header.getTimeSeconds(),
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
}
