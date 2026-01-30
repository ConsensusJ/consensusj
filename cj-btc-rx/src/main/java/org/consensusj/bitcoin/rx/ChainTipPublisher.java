/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
