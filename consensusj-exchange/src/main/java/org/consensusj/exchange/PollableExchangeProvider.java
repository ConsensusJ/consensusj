package org.consensusj.exchange;

import org.consensusj.exchange.rx.ExchangeRateUpdate;

/**
 *
 */
public interface PollableExchangeProvider {
    ExchangeRateUpdate getUpdate(CurrencyUnitPair pair);
}
