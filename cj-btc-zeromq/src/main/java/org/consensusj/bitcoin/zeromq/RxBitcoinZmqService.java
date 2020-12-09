package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.BlockUtil;
import org.consensusj.bitcoin.rx.RxBlockchainService;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.consensusj.bitcoin.zeromq.BitcoinZmqMessage.Topic.*;

/**
 * Service to listen for ZMQ messages from multiple TCP ports. Uses Bitcoin Core JSON-RPC to find
 * the TCP address of the required services.
 */
public class RxBitcoinZmqService implements RxBlockchainService, BitcoinZmqService, AutoCloseable {

    private final BitcoinClient client;
    private final BitcoinZmqService blockService;
    private final BitcoinZmqService txService;

    private final Observable<Transaction> observableRawTx;
    private final Observable<Block> observableRawBlock;

    public RxBitcoinZmqService(NetworkParameters networkParameters, URI rpcUri, String rpcUser, String rpcPassword) {
        client = new BitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword);

        BitcoinZmqPortFinder portFinder = new BitcoinZmqPortFinder(client);

        Optional<URI> blockServiceURI = portFinder.findPort(rawblock);
        Optional<URI> txServiceURI = portFinder.findPort(rawtx);

        if (!(blockServiceURI.isPresent() && txServiceURI.isPresent())) {
            throw new RuntimeException("Bitcoin Core configuration error");
        }

        if (blockServiceURI.get().equals(txServiceURI.get())) {
            // URIs are the same we can use a single connection
            blockService = new RxBitcoinSinglePortZmqService(networkParameters, blockServiceURI.get(), rawblock, rawtx);
            txService = blockService;
        } else {
            blockService = new RxBitcoinSinglePortZmqService(networkParameters, blockServiceURI.get(), rawblock);
            txService = new RxBitcoinSinglePortZmqService(networkParameters, txServiceURI.get(), rawtx);
        }

        observableRawBlock = blockService.observableBlock().startWith(getInitialBlock());
        observableRawTx = txService.observableTransaction();
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return client.getNetParams();
    }

    @Override
    public Observable<Transaction> observableTransaction() {
        return observableRawTx;
    }

    @Override
    public Observable<Sha256Hash> observableTransactionHash() {
        return observableRawTx.map(Transaction::getTxId);
    }

    @Override
    public Observable<Block> observableBlock() {
        return observableRawBlock;
    }

    @Override
    public Observable<Sha256Hash> observableBlockHash() {
        return observableRawBlock.map(Block::getHash);
    }

    @Override
    public Observable<Integer> observableBlockHeight() {
        return observableRawBlock.map(BlockUtil::blockHeightFromCoinbase);
    }

    @Override
    public void close() throws Exception {
        blockService.close();
        txService.close();
    }

    private Single<Block> getInitialBlock() {
        return Single.fromCompletionStage(getInitialBlockViaRPC());
    }

    private CompletableFuture<Block> getInitialBlockViaRPC() {
        return getBlockChainInfoAsync().thenCompose(info -> getBlockAsync(info.getBestBlockHash()));
    }

    private CompletableFuture<Block> getBlockAsync(Sha256Hash blockHash) {
        return client.supplyAsync(() -> client.getBlock(blockHash));
    }

    private CompletableFuture<BlockChainInfo> getBlockChainInfoAsync() {
        return client.supplyAsync(client::getBlockChainInfo);
    }
}
