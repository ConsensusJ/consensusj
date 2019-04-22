package org.consensusj.exchange

import spock.lang.Ignore

@Ignore("this is really an integration test")
class ItBitXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    BaseXChangeExchangeRateProvider createProvider() {
        return new DynamicXChangeRateProvider("org.knowm.xchange.itbit.v1.ItBitExchange", "BTC/USD", "BTC/EUR")
    }
}