package org.consensusj.exchange;

import javax.money.convert.ExchangeRate;

/**
 * Data object passed to `ExchangeRateObserver` on rate updates
 * TODO: This should become a JavaMoney version of the XChange Ticker
 * It should have the JavaMoney currency types, all the rates,
 * An optional server timestamp and a mandatory client timestamp
 * TODO: And it should probably be called "Update" rather than "Change" -- if the timestamp
 * was updated but the exchange rate didn't, users still want to know.
 */
@Deprecated
public class ExchangeRateChange {
    public final ExchangeRate rate;
    public final Long timestamp;

    public ExchangeRateChange(ExchangeRate rate, long timestamp) {
        this.rate = rate;
        this.timestamp = timestamp;
    }
}
