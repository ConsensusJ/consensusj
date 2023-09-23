package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.ChainTipClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides {@link ChainTipService} a using a {@link BitcoinClient} and a polling interval.
 * This can be used as a fallback if ZeroMQ is not available.
 */
public class PollingChainTipServiceImpl implements ChainTipService, ChainTipClient, RxJsonRpcClient, Closeable {
    private static final Logger log = LoggerFactory.getLogger(PollingChainTipServiceImpl.class);
    private final BitcoinClient client;
    private final Observable<Long> interval;
    // How will we properly use backpressure here?
    private final Flowable<ChainTip> chainTipSource;
    private Disposable chainTipSubscription;
    private final FlowableProcessor<ChainTip> chainTipProcessor = BehaviorProcessor.create();

    /**
     * Construct from a {@link BitcoinClient} or subclass and a polling interval
     * @param bitcoinClient a client instance
     * @param interval a polling interval
     */
    public PollingChainTipServiceImpl(BitcoinClient bitcoinClient, Observable<Long> interval) {
        client = bitcoinClient;
        this.interval = interval;
        log.info("Constructing polling ChainTipService: {}, {}", client.getNetwork().id(), client.getServerURI());
        chainTipSource = pollForDistinctChainTip();
    }

    public PollingChainTipServiceImpl(BitcoinClient bitcoinClient) {
        this(bitcoinClient, Observable.interval(2,10, TimeUnit.SECONDS));
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

    /**
     * Provide a polling interval
     *
     * @return polling interval with desired frequency for polling for new ChainTips.
     */
    public Observable<Long> getPollingInterval() {
        return interval;
    }

    /**
     * Using a polling interval provided by {@link #getPollingInterval()} provide a
     * stream of distinct {@link ChainTip}s.
     *
     * @return A stream of distinct {@code ChainTip}s.
     */
    public Flowable<ChainTip> pollForDistinctChainTip() {
        return getPollingInterval()
                .doOnNext(t -> log.debug("got interval"))
                .flatMapMaybe(t -> this.currentChainTipMaybe())
                .doOnNext(tip -> log.debug("blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                .distinctUntilChanged(ChainTip::getHash)
                .doOnNext(tip -> log.info("** NEW ** blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                // ERROR backpressure strategy is compatible with BehaviorProcessor since it subscribes to MAX items
                .toFlowable(BackpressureStrategy.ERROR);
    }

    /**
     * Get the active chain tip if there is one (useful for polling clients)
     *
     * @return The active ChainTip if available (onSuccess) otherwise onComplete (if not available) or onError (if error occurred)
     */
    private Maybe<ChainTip> currentChainTipMaybe() {
        return pollOnceAsync(this::getChainTipsAsync)
                .mapOptional(ChainTip::findActiveChainTip);
    }

    @Override
    public void close() {
        chainTipSubscription.dispose();
    }

    @Override
    public CompletableFuture<List<ChainTip>> getChainTipsAsync() {
        return client.getChainTipsAsync();
    }

    @Override
    @Deprecated
    public List<ChainTip> getChainTips() throws JsonRpcStatusException, IOException {
        return client.syncGet(client.getChainTipsAsync());
    }
}
