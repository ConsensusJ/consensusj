package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test")
class BitfinexXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    def createProvider() {
        return new BitfinexXChangeRateProvider()
    }
}