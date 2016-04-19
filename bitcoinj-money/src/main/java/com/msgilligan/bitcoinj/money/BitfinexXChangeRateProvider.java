package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.currency.CurrencyPair;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class BitfinexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BitfinexXChangeRateProvider() {
        super(BitfinexExchange.class, CurrencyPair.BTC_USD, "BTC", "USD");
    }
}
