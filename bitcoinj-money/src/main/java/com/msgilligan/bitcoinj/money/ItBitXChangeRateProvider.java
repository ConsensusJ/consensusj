package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.itbit.v1.ItBitExchange;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class ItBitXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public ItBitXChangeRateProvider() {
        super(ItBitExchange.class, "BTC", "USD", "EUR");
    }

    protected CurrencyPair xchangePair(String base, String target) {
        if (base.equals("BTC")) {
            base = "XBT";
        }
        return new CurrencyPair(base, target);
    }
}
