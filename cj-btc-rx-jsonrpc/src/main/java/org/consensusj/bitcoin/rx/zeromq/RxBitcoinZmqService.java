package org.consensusj.bitcoin.rx.zeromq;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.util.BlockUtil;
import org.consensusj.bitcoin.rx.RxBlockchainService;

import java.io.Closeable;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 *  Add conversion to bitcoinj-types to {@code RxBitcoinZmqBinaryService}. Also
 *  uses the JSON-RPC client to fetch an initial {@link Block} so subscribers don't
 *  have to wait ~ten minutes for one.
 */
public class RxBitcoinZmqService extends RxBitcoinZmqBinaryService implements RxBlockchainService, ChainTipService, Closeable {
    private final Context bitcoinContext;
    private final BitcoinSerializer bitcoinSerializer;

    private final FlowableProcessor<ChainTip> flowableChainTip = BehaviorProcessor.create();
    private final Disposable blockSubscription;

    public RxBitcoinZmqService(NetworkParameters networkParameters, URI rpcUri, String rpcUser, String rpcPassword) {
        this(new BitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword));
    }

    public RxBitcoinZmqService(BitcoinClient client) {
        super(client);
        bitcoinContext = new Context(networkParameters);
        bitcoinSerializer = networkParameters.getSerializer(false);
        blockSubscription = blockPublisher()
                .flatMapSingle(this::chainTipFromBlock)
                .distinctUntilChanged(ChainTip::getHash)
                .subscribe(this::onNextChainTip, flowableChainTip::onError);
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    @Override
    public Flowable<Transaction> transactionPublisher() {
        return transactionBinaryPublisher()
                .map(bytes -> new Transaction(networkParameters, bytes));   // Deserialize to Transaction
    }

    @Override
    public Flowable<Sha256Hash> transactionHashPublisher() {
        return transactionHashBinaryPublisher()
                .map(Sha256Hash::wrap);            // Deserialize to Sha256Hash
    }

    @Override
    public Flowable<Block> blockPublisher() {
        return blockBinaryPublisher()
                .map(bitcoinSerializer::makeBlock)  // Deserialize to bitcoinj Block
                .startWith(getLatestBlockViaRpc()); // Use JSON-RPC client to fetch an initial block
    }
    
    @Override
    public Flowable<Sha256Hash> blockHashPublisher() {
        return blockHashBinaryPublisher()
                .map(Sha256Hash::wrap);             // Deserialize to Sha256Hash
    }

    @Override
    public Flowable<Integer> blockHeightPublisher() {
        return chainTipPublisher()
                .map(ChainTip::getHeight);          // Extract block height
    }

    @Override
    public Flowable<ChainTip> chainTipPublisher() {
        return flowableChainTip;
    }

    @Override
    public void close()  {
        super.close();
        blockSubscription.dispose();
    }

    // For setting breakpoints
    void onNextChainTip(ChainTip tip) {
        flowableChainTip.onNext(tip);
    }

    private Single<ChainTip> chainTipFromBlock(Block block) {
        int height = BlockUtil.blockHeightFromCoinbase(block);
        if (height != -1) {
            return Single.just(new ChainTip(height, block.getHash(), 0, "active"));
        } else {
            return getLatestChainTipViaRpc();
        }
    }

    private Single<ChainTip> getLatestChainTipViaRpc() {
        return Single.defer(() -> Single.fromCompletionStage(getChainTipAsync()));
    }

    /**
     * Get the latest (aka "best") {@link Block} via JSON-RPC.
     * 
     * @return A "hot" {@code Single} that will fetch the "best" block via JSON-RPC
     */
    private Single<Block> getLatestBlockViaRpc() {
        return Single.defer(() -> Single.fromCompletionStage(getBestBlock()));
    }

    private CompletableFuture<Block> getBestBlock() {
        return getChainTipAsync().thenCompose(tip -> getBlockAsync(tip.getHash()));
    }

    private CompletableFuture<Block> getBlockAsync(Sha256Hash blockHash) {
        return client.supplyAsync(() -> client.getBlock(blockHash));
    }

    private CompletableFuture<ChainTip> getChainTipAsync() {
        return client.supplyAsync(client::getChainTips)
                .thenApply(this::getActiveChainTip);
                //.whenComplete((tip, error) -> flowableChainTip.onNext(tip));
    }

    private ChainTip getActiveChainTip(List<ChainTip> chainTips) {
        return chainTips.stream().filter(tip -> tip.getStatus().equals("active")).findFirst().orElseThrow(() -> new RuntimeException("No active chaintip"));
    }

    /**
     * This method might be useful if we end up needing bitcoinj Context-aware threads
     * for deserializing binary messages to bitcoinj types.
     */
    private Thread createZMQThread(Runnable runnable) {
        return new Thread(() -> {
            Context.propagate(this.bitcoinContext);
            runnable.run();
        }, "ZMQ Bitcoin Thread (" + networkParameters.getId()  +")");
    }

}
