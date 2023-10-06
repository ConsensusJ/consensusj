package org.consensusj.bitcoin.rx;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Marker type for {@code Publisher<ChainTip>}. In a future release this may use {@link java.util.concurrent.Flow.Publisher}.
 * Because of type erasure in Java generics we need this to strongly type parameters that require a {@code Publisher<ChainTip>}.
 */
public interface ChainTipPublisher extends Publisher<ChainTip> {
    /**
     * Adapt a {@code Publisher<ChainTip}
     * @param publisher to wrap
     * @return wrapped publisher
     */
    static ChainTipPublisher of(Publisher<ChainTip> publisher) {
        return new Wrapper(publisher);
    }
    
    class Wrapper implements ChainTipPublisher {
        private final Publisher<ChainTip> publisher;

        Wrapper(Publisher<ChainTip> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void subscribe(Subscriber<? super ChainTip> s) {
            publisher.subscribe(s);
        }
    }
}
