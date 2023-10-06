package org.consensusj.bitcoin.rx;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.reactivestreams.Publisher;

/**
 * A publisher of {@link ChainTip}
 */
public interface ChainTipService {
    /**
     * This method will give you a stream of ChainTips
     *
     * @return A Publisher for the sequence
     */
    ChainTipPublisher chainTipPublisher();
}
