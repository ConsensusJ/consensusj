package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test")
class CoinbaseXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    BaseXChangeExchangeRateProvider createProvider() {
        return new DynamicXChangeRateProvider("org.knowm.xchange.coinbase.CoinbaseExchange", "BTC/USD")
    }
}