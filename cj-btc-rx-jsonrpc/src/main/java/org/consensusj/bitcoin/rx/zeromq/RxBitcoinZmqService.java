package org.consensusj.bitcoin.rx.zeromq;

import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;
import org.consensusj.bitcoin.rx.RxBlockchainService;
import org.consensusj.bitcoinj.util.BlockUtil;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;

import java.io.Closeable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 *  Add conversion to bitcoinj-types to {@code RxBitcoinZmqBinaryService}. Also
 *  uses the JSON-RPC client to fetch an initial {@link Block} so subscribers don't
 *  have to wait ~ten minutes for one.
 */
public class RxBitcoinZmqService extends RxBitcoinZmqBinaryService implements RxBlockchainService, ChainTipService, Closeable {

    private final FlowableProcessor<ChainTip> flowableChainTip = BehaviorProcessor.create();
    private final Disposable blockSubscription;

    public RxBitcoinZmqService(Network network, URI rpcUri, String rpcUser, String rpcPassword) {
        this(new RxBitcoinClient(network, rpcUri, rpcUser, rpcPassword));
    }

    public RxBitcoinZmqService(BitcoinClient client) {
        super(client);
        blockSubscription = Flowable.fromPublisher(blockPublisher())
                .flatMap(this::activeChainTipFromBestBlockPublisher)
                .distinctUntilChanged(ChainTip::getHash)
                .subscribe(this::onNextChainTip, flowableChainTip::onError, flowableChainTip::onComplete);
    }

    @Override
    public Publisher<Transaction> transactionPublisher() {
        return Flowable.fromPublisher(transactionBinaryPublisher())
                .map(bytes -> Transaction.read(ByteBuffer.wrap(bytes)));   // Deserialize to Transaction
    }

    @Override
    public Publisher<Sha256Hash> transactionHashPublisher() {
        return Flowable.fromPublisher(transactionHashBinaryPublisher())
                .map(Sha256Hash::wrap);            // Deserialize to Sha256Hash
    }

    @Override
    public Publisher<Block> blockPublisher() {
        return Flowable.fromPublisher(blockBinaryPublisher())
                .map(ByteBuffer::wrap)
                .map(Block::read)  // Deserialize to bitcoinj Block
                .startWith(RxJsonRpcClient.defer(client::getBestBlock)); // Use JSON-RPC client to fetch an initial block
    }
    
    @Override
    public Publisher<Sha256Hash> blockHashPublisher() {
        return Flowable.fromPublisher(blockHashBinaryPublisher())
                .map(Sha256Hash::wrap);             // Deserialize to Sha256Hash
    }

    @Override
    public Publisher<Integer> blockHeightPublisher() {
        return Flowable.fromPublisher(chainTipPublisher())
                .map(ChainTip::getHeight);          // Extract block height
    }

    @Override
    public Publisher<ChainTip> chainTipPublisher() {
        return flowableChainTip;
    }

    @Override
    public void close()  {
        super.close();
        blockSubscription.dispose();
    }
    private Publisher<ChainTip> activeChainTipFromBestBlockPublisher(Block block) {
        return Flowable.defer(() -> Flowable.fromCompletionStage(activeChainTipFromBestBlock(block)));
    }

    /**
     * Convert best {@link Block} to active {@link ChainTip}. If BIP34 is activated this
     * is purely computational, otherwise I/O is required to fetch the {@code ChainTip}
     *
     * @param block Input best block
     * @return active ChainTip assuming parameter block is the "best block"
     */
    private CompletableFuture<ChainTip> activeChainTipFromBestBlock(Block block) {
        int height = BlockUtil.blockHeightFromCoinbase(block);
        if (height != -1) {
            return CompletableFuture.completedFuture(ChainTip.ofActive(height, block.getHash()));
        } else {
            return client.getChainTipsAsync()
                    .thenApply(ChainTip::findActiveChainTipOrElseThrow);
        }
    }

    // For setting breakpoints
    void onNextChainTip(ChainTip tip) {
        flowableChainTip.onNext(tip);
    }
}
