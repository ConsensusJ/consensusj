package org.consensusj.exchange.rx;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.consensusj.exchange.CurrencyUnitPair;
import org.consensusj.exchange.PollableExchangeProvider;

/**
 *
 */
public class ObservablePair extends Observable<ExchangeRateUpdate> {
    private final PollableExchangeProvider exchangeRateProvider;
    public final CurrencyUnitPair pair;
    public volatile long pollCount = 0;

    private final Observable<ExchangeRateUpdate> observablePairUpdates;

    public ObservablePair(PollableExchangeProvider exchangeRateProvider, CurrencyUnitPair pair, Observable<Long> interval) {
        this.exchangeRateProvider = exchangeRateProvider;
        this.pair = pair;
        observablePairUpdates = interval.map(this::poll).publish().refCount();
    }

    private ExchangeRateUpdate poll(Long zeroLong) {
        System.out.println("Polling count: " + pollCount);
        pollCount++;
        return exchangeRateProvider.getUpdate(pair);
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super ExchangeRateUpdate> observer) {
        observablePairUpdates.subscribe(observer);
    }
}
