package org.consensusj.exchange

import spock.lang.Ignore

@Ignore("this is really an integration test")
class ItBitXChangeRateProviderSpec extends AbstractXChangeRateProviderSpec {
    @Override
    BaseXChangeExchangeRateProvider createProvider() {
        return new BaseXChangeExchangeRateProvider("org.knowm.xchange.itbit.ItBitExchange", null, "BTC/USD", "BTC/EUR")
    }
}