package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.currency.CurrencyPair;

import java.util.concurrent.ScheduledExecutorService;

/**
 *  Bitfinex ExchangeRateProvider using XChange library
 */
public class ItBitXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD", "BTC/EUR"};
    static private final String xchangeClassName = "org.knowm.xchange.itbit.v1.ItBitExchange";

    public ItBitXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }
    
    public ItBitXChangeRateProvider() {
        super(xchangeClassName, null, pairsConvert(pairs));
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
