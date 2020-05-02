package org.consensusj.exchange.rx;

import org.consensusj.exchange.CurrencyUnitPair;

/**
 *
 */
public interface ReactiveExchange {
    String getName();
    ObservablePair getObservablePair(CurrencyUnitPair pair);
}
