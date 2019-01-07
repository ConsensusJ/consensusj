package com.msgilligan.bitcoinj.money;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Bitfinex ExchangeRateProvider using XChange library
 * @deprecated Use DynamicXChangeRateProvider
 */
@Deprecated
public class BitfinexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD"};
    static private final String xchangeClassName = "org.knowm.xchange.bitfinex.v1.BitfinexExchange";

    public BitfinexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }

    public BitfinexXChangeRateProvider() {
        super(xchangeClassName, null, pairsConvert(pairs));
    }
}
