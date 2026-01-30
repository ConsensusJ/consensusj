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
package org.consensusj.exchange.rx;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.consensusj.exchange.CurrencyUnitPair;

import java.util.function.Supplier;

/**
 * Implements ObservablePair using an exchange rate polling function and an interval
 */
public class PolledObservablePair extends Observable<ExchangeRateUpdate> implements ObservablePair {
    private final CurrencyUnitPair pair;
    private final Observable<ExchangeRateUpdate> observablePairUpdates;

    /**
     *
     * @param pair The currency pair to observe
     * @param rateSupplier A polling Supplier function (for a single pair)
     * @param interval An interval (or equivalent) to provide clock ticks to trigger polling
     */
    public PolledObservablePair(CurrencyUnitPair pair, Supplier<ExchangeRateUpdate> rateSupplier, Observable<Long> interval) {
        this.pair = pair;
        observablePairUpdates = interval
                .map(l -> rateSupplier.get())   // Use the Supplier function to do the polling
                .replay(1)            // Create a ConnectableObservable that can start and stop polling
                .refCount();                    // Create a refCount-tracking observable to share the ConnectableObservable
    }

    @Override
    public CurrencyUnitPair getPair() {
        return pair;
    }
    
    @Override
    protected void subscribeActual(@NonNull Observer<? super ExchangeRateUpdate> observer) {
        observablePairUpdates.subscribe(observer);
    }
}
