package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test and Bitfinex rate limit is really low")
class BitfinexXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    def createProvider() {
        return new BitfinexXChangeRateProvider()
    }
}