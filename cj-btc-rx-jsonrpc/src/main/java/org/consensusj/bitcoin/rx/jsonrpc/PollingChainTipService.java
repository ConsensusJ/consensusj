package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface with {@link PollingChainTipService#pollForDistinctChainTip()} method.
 */
public interface PollingChainTipService extends RxJsonChainTipClient {
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
    default Flowable<ChainTip> pollForDistinctChainTip() {
        return getPollingInterval()
                .doOnNext(t -> log.debug("got interval"))
                .flatMapMaybe(t -> this.currentChainTipMaybe())
                .doOnNext(tip -> log.debug("blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                .distinctUntilChanged(ChainTip::getHash)
                .doOnNext(tip -> log.info("** NEW ** blockheight, blockhash = {}, {}", tip.getHeight(), tip.getHash()))
                // ERROR backpressure strategy is compatible with BehaviorProcessor since it subscribes to MAX items
                .toFlowable(BackpressureStrategy.ERROR);
    }
}
