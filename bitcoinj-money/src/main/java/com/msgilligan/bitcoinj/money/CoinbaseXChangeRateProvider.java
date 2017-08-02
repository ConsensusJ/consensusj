package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.coinbase.CoinbaseExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Coinbase ExchangeRateProvider using XChange library
 */
public class CoinbaseXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD"};

    public CoinbaseXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(CoinbaseExchange.class, scheduledExecutorService, pairs);
    }

    public CoinbaseXChangeRateProvider() {
        super(CoinbaseExchange.class, pairs);
    }
}
