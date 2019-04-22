package org.consensusj.exchange

import spock.lang.Ignore

@Ignore("this is really an integration test and Bitfinex rate limit is really low")
class BitfinexXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    BaseXChangeExchangeRateProvider createProvider() {
        return new DynamicXChangeRateProvider("org.knowm.xchange.bitfinex.v1.BitfinexExchange", "BTC/USD")
    }
}