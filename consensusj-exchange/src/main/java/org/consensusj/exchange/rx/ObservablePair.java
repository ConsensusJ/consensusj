package org.consensusj.exchange.rx;

import io.reactivex.rxjava3.core.ObservableSource;
import org.consensusj.exchange.CurrencyUnitPair;

/**
 * A source of exchange rate updates for a currency pair
 */
public interface ObservablePair extends ObservableSource<ExchangeRateUpdate> {
    CurrencyUnitPair getPair();
}
