package org.consensusj.exchange;

/**
 * Implement this interface to register with `ObservableExchangeRateProvider` for rate updates
 * TODO: Should we replace this with RxJava? Yes! Coming soon!
 */
public interface ExchangeRateObserver {
    void onExchangeRateChange(ExchangeRateChange change);
}
