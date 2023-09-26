package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.BackpressureStrategy;
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
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// TODO: Rewrite using ScheduledThreadExecutor (instead of an Observable interval) and SubmissionPublisher instead of FlowableProcessor.
//  Then we can merge it into BitcoinClient (as a default component.)
//  We may need to use atomic object (or something else?) to replace distinctUntilChanged
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
    Flowable<ChainTip> pollForDistinctChainTip() {
        return getPollingInterval()
                // ERROR backpressure strategy is compatible with BehaviorProcessor since it subscribes to MAX items
                .toFlowable(BackpressureStrategy.ERROR)
                .doOnNext(t -> log.debug("got interval"))
                .flatMap(t -> this.currentChainTipMaybe())
                .doOnNext(tip -> log.debug("blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                .distinctUntilChanged(ChainTip::getHash)
                .doOnNext(tip -> log.info("** NEW ** blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()));
    }

    /**
     * Get the active chain tip if there is one (useful for polling clients)
     *
     * @return The active ChainTip if available (onSuccess) otherwise onComplete (if not available) or onError (if error occurred)
     */
    private Publisher<ChainTip> currentChainTipMaybe() {
        return pollOnceAsPublisher(client::getChainTipsAsync, TransientErrorFilter.of(this::isTransientError, this::logError))
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

    private boolean isTransientError(Throwable t) {
        return t instanceof IOError;
    }
    private void logError(Throwable throwable) {
        log.error("Exception in RPCCall", throwable);
    }
}
