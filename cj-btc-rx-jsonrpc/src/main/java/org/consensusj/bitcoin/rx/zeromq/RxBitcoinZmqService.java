package org.consensusj.bitcoin.rx.zeromq;

import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;
import org.consensusj.bitcoin.rx.RxBlockchainService;

import java.io.Closeable;
import java.net.URI;
import java.nio.ByteBuffer;


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

    public RxBitcoinZmqService(RxBitcoinClient client) {
        super(client);
        blockSubscription = blockPublisher()
                .flatMapSingle(client::activeChainTipFromBestBlock)
                .distinctUntilChanged(ChainTip::getHash)
                .subscribe(this::onNextChainTip, flowableChainTip::onError, flowableChainTip::onComplete);
    }

    @Override
    public Flowable<Transaction> transactionPublisher() {
        return transactionBinaryPublisher()
                .map(bytes -> Transaction.read(ByteBuffer.wrap(bytes)));   // Deserialize to Transaction
    }

    @Override
    public Flowable<Sha256Hash> transactionHashPublisher() {
        return transactionHashBinaryPublisher()
                .map(Sha256Hash::wrap);            // Deserialize to Sha256Hash
    }

    @Override
    public Flowable<Block> blockPublisher() {
        return blockBinaryPublisher()
                .map(ByteBuffer::wrap)
                .map(Block::read)  // Deserialize to bitcoinj Block
                .startWith(client.getBestBlockViaRpc()); // Use JSON-RPC client to fetch an initial block
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
}
