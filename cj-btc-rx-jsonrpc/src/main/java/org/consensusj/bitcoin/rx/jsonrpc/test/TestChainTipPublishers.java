package org.consensusj.bitcoin.rx.jsonrpc.test;

import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.rx.ChainTipPublisher;

/**
 *  Useful for testing.
 */
public class TestChainTipPublishers {
    /**
     * @return A {@link ChainTipPublisher} that never emits items and never closes.
     */
    public static ChainTipPublisher never() {
        return ChainTipPublisher.of(Flowable.never());
    }
}
