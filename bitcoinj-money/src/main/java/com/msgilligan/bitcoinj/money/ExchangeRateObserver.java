package com.msgilligan.bitcoinj.money;

/**
 * Implement this interface to register with `ObservableExchangeRateProvider` for rate updates
 * TODO: Should we replace this with RxJava?
 */
public interface ExchangeRateObserver {
    void onExchangeRateChange(ExchangeRateChange change);
}
