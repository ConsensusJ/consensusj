package org.consensusj.bitcoin.rx.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link PollingChainTipService} using a {@link BitcoinClient} and a polling interval.
 * This can be used as a fallback if ZeroMQ is not available
 */
public class PollingChainTipServiceImpl implements Closeable, PollingChainTipService {
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

    @Override
    public Observable<Long> getPollingInterval() {
        return interval;
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
