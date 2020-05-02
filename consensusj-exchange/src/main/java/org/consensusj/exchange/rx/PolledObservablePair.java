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
