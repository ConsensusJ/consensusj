package com.msgilligan.bitcoinj.money;

import org.knowm.xchange.dto.marketdata.Ticker;

/**
 * Implement this interface to register with `ObservableExchangeRateProvider` for rate upates
 */
public interface ExchangeRateObserver {
    void onExchangeRateChange(ExchangeRateChange change);
}
