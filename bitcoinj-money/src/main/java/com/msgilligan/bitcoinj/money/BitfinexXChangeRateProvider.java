package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class BitfinexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD"};

    public BitfinexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(BitfinexExchange.class, scheduledExecutorService, pairs);
    }

    public BitfinexXChangeRateProvider() {
        super(BitfinexExchange.class, pairs);
    }
}
