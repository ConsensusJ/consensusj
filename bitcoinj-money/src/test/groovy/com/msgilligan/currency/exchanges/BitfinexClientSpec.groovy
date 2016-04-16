package com.msgilligan.currency.exchanges

import spock.lang.Ignore
import spock.lang.Specification


@Ignore("this is really an integration test")
class BitfinexClientSpec extends Specification {
    def "can get a price"() {
        given:
        def client = new BitfinexClient();

        when:
        BigDecimal price = client.getPrice()

        then:
        price > 0
    }

}