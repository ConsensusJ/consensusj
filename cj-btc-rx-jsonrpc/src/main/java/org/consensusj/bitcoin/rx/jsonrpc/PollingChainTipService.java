package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.jsonrpc.ChainTipClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface with {@link PollingChainTipService#pollForDistinctChainTip()} method.
 * @deprecated Use the {@link PollingChainTipServiceImpl} implementation
 */
@Deprecated
public interface PollingChainTipService extends ChainTipService, ChainTipClient, RxJsonRpcClient {
    Logger log = LoggerFactory.getLogger(PollingChainTipService.class);

    /**
     * Implement this method to provide a polling interval
     *
     * @return polling interval with desired frequency for polling for new ChainTips.
     */
    Observable<Long> getPollingInterval();

    /**
     * Using a polling interval provided by {@link PollingChainTipService#getPollingInterval()} provide a
     * stream of distinct {@link ChainTip}s.
     *
     * @return A stream of distinct {@code ChainTip}s.
     */
    default Publisher<ChainTip> pollForDistinctChainTip() {
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
}
