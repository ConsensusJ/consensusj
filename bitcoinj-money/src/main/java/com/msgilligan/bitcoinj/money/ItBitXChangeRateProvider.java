package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.itbit.v1.ItBitExchange;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class ItBitXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public ItBitXChangeRateProvider() {
        super(ItBitExchange.class, new CurrencyPair("XBT", "USD"));
    }
}
