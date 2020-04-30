package org.consensusj.exchange;

import org.consensusj.exchange.rx.ExchangeRateUpdate;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.NumberValue;

/**
 *
 */
public class PollableIdentityExchangeProvider implements PollableExchangeProvider {
    public final CurrencyUnitPair identityPair = new CurrencyUnitPair("USD", "USD");
    public final NumberValue fixedRate = DefaultNumberValue.of(1);
    public final ExchangeRateUpdate fixedUpdate = new ExchangeRateUpdate(identityPair, fixedRate, 0);

    @Override
    public ExchangeRateUpdate getUpdate(CurrencyUnitPair pair) {
        return fixedUpdate;
    }
}
