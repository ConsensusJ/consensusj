package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class BitfinexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BitfinexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(BitfinexExchange.class, scheduledExecutorService, "BTC/USD");
    }

    public BitfinexXChangeRateProvider() {
        super(BitfinexExchange.class, "BTC/USD");
    }
}
