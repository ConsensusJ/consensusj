package org.consensusj.bitcoin.rx.jsonrpc;

import com.msgilligan.bitcoinj.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableInterval;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This can be used as a fallback if ZeroMQ is not available
 */
public class PollingChainTipService implements ChainTipService, Closeable {
    private static final Logger log = LoggerFactory.getLogger(PollingChainTipService.class);
    private final RxBitcoinClient client;
    private final Observable<Long> interval;
    // How will we properly use backpressure here?
    private final Flowable<ChainTip> chainTipSource;
    private Disposable chainTipSubscription;
    private final FlowableProcessor<ChainTip> chainTipProcessor = BehaviorProcessor.create();

    public PollingChainTipService(RxBitcoinClient rxJsonRpcClient, Observable<Long> interval) {
        client = rxJsonRpcClient;
        this.interval = interval;
        log.info("Constructing polling ChainTipService: {}, {}", client.getNetParams().getId(), client.getServerURI());
        chainTipSource = pollForDistinctChainTip();
    }

    public PollingChainTipService(RxBitcoinClient rxJsonRpcClient) {
        this(rxJsonRpcClient, ObservableInterval.interval(2,10, TimeUnit.SECONDS));
    }

    public synchronized void start() {
        if (chainTipSubscription == null) {
            chainTipSubscription = chainTipSource.subscribe(chainTipProcessor::onNext, chainTipProcessor::onError, chainTipProcessor::onComplete);
        }
    }

    @Override
    public Publisher<ChainTip> chainTipPublisher() {
        start();
        return chainTipProcessor;
    }

    @Override
    public void close() {
        chainTipSubscription.dispose();
    }

    private Flowable<ChainTip> pollForDistinctChainTip() {
        return interval
                .doOnNext(t -> log.debug("got interval"))
                .flatMapMaybe(t -> this.currentChainTipMaybe())
                .doOnNext(tip -> log.debug("blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                .distinctUntilChanged(ChainTip::getHash)
                .doOnNext(tip -> log.info("** NEW ** blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                // ERROR backpressure strategy is compatible with BehaviorProcessor since it subscribes to MAX items
                .toFlowable(BackpressureStrategy.ERROR);
    }

    private Maybe<ChainTip> currentChainTipMaybe() {
        return client.pollOnce(client::getChainTips)
                .mapOptional(this::getActiveChainTip);
    }

    private Optional<ChainTip> getActiveChainTip(List<ChainTip> chainTips) {
        return chainTips.stream().filter(tip -> tip.getStatus().equals("active")).findFirst();
    }
}
