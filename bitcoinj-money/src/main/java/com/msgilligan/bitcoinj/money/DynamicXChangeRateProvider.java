package com.msgilligan.bitcoinj.money;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Generic XChange ExchangeRateProvider that can wrap any XChange class
 */
public class DynamicXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public DynamicXChangeRateProvider(String exchangeClassName, ScheduledExecutorService scheduledExecutorService, CurrencyUnitPair... pairs) {
        super(exchangeClassName, scheduledExecutorService, pairs);
    }

    public DynamicXChangeRateProvider(String exchangeClassName, CurrencyUnitPair... pairs) {
        this(exchangeClassName, null, pairs);
    }

}
