package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test and Bitfinex rate limit is really low")
class BitfinexXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    BaseXChangeExchangeRateProvider createProvider() {
        return new BitfinexXChangeRateProvider()
    }
}