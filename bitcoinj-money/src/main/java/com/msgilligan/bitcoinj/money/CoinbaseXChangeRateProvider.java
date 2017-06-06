package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.coinbase.CoinbaseExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Coinbase ExchangeRateProvider using XChange library
 */
public class CoinbaseXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public CoinbaseXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(CoinbaseExchange.class, scheduledExecutorService, "BTC/USD");
    }

    public CoinbaseXChangeRateProvider() {
        super(CoinbaseExchange.class, "BTC/USD");
    }
}
