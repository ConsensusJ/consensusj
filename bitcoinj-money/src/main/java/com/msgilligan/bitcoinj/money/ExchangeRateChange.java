package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.dto.marketdata.Ticker;

import javax.money.convert.ExchangeRate;

/**
 * Data object passed to `ExchangeRateObserver` on rate updates
 * TODO: This should become a JavaMoney version of the XChange Ticker
 * It should have the JavaMoney currency types, all the rates,
 * An optional server timestamp and a mandatory client timestamp
 * TODO: And it should probably be called "Update" rather than "Change" -- if the timestamp
 * was updated but the exchange rate didn't, users still want to know.
 */
public class ExchangeRateChange {
    public ExchangeRate rate;
    public Long timestamp;
    //Ticker ticker;

    public ExchangeRateChange(ExchangeRate rate, long timestamp) {
        this.rate = rate;
        this.timestamp = timestamp;
    }
}
