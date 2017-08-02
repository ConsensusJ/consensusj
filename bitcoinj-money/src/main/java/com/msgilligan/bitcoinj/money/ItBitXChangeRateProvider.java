package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.itbit.v1.ItBitExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class ItBitXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD", "BTC/EUR"};

    public ItBitXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(ItBitExchange.class, scheduledExecutorService, pairs);
    }
    
    public ItBitXChangeRateProvider() {
        super(ItBitExchange.class, pairs);
    }

    @Override
    protected CurrencyPair xchangePair(CurrencyUnitPair pair) {
        String base = pair.getBase().getCurrencyCode();
        if (base.equals("BTC")) {
            base = "XBT";
        }
        return new CurrencyPair(base, pair.getTarget().getCurrencyCode());
    }
}
