package com.msgilligan.bitcoinj.money;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Coinbase ExchangeRateProvider using XChange library
 * @deprecated Use DynamicXChangeRateProvider
 */
@Deprecated
public class CoinbaseXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD"};
    static private final String xchangeClassName = "org.knowm.xchange.coinbase.CoinbaseExchange";

    public CoinbaseXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }

    public CoinbaseXChangeRateProvider() {
        super(xchangeClassName, null, pairsConvert(pairs));
    }
}
