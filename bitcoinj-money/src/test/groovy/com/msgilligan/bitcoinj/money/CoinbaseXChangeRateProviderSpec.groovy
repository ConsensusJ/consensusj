package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test")
class CoinbaseXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    def createProvider() {
        return new CoinbaseXChangeRateProvider()
    }
}