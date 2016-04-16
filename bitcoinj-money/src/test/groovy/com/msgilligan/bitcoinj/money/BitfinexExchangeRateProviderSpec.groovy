package com.msgilligan.bitcoinj.money

import spock.lang.Ignore
import spock.lang.Specification


@Ignore("this is really an integration test")
class BitfinexExchangeRateProviderSpec extends Specification {
    def "the most basic function works"() {
        given:
        def provider = new BitfinexExchangeRateProvider()

        when:
        def rate = provider.getExchangeRate("USD", "BTC")

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

}