package com.msgilligan.bitcoinj.money;

import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;

/**
 *
 */
public interface ObservableExchangeRateProvider extends ExchangeRateProvider {
    void registerExchangeRateObserver(ExchangeRate rate, ExchangeRateObserver observer);
    void start();
    void stop();

}
