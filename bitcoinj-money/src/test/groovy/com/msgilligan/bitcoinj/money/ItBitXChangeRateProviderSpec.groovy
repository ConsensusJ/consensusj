package com.msgilligan.bitcoinj.money

import spock.lang.Ignore

@Ignore("this is really an integration test")
class ItBitXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    def createProvider() {
        return new ItBitXChangeRateProvider()
    }

    def "can list currency codes"() {
        when:
        def markets = provider.exchange.exchangeMetaData.currencyPairs

        then:
        markets.size() > 0
    }

}