package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.coinbase.CoinbaseExchange;

/**
 *  Coinbase ExchangeRateProvider using XChange library
 */
public class CoinbaseXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public CoinbaseXChangeRateProvider() {
        super(CoinbaseExchange.class, "BTC/USD");
    }
}
