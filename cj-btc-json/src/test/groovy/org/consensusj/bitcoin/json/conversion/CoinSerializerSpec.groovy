package org.consensusj.bitcoin.json.conversion

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import spock.lang.Unroll

/**
 * Spock Spec to test CoinSerializer
 */
class CoinSerializerSpec extends BaseObjectMapperSpec {
    @Unroll
    def "fragment #value serializes as #expectedResult"() {
        when:
        def result = mapper.writeValueAsString(value)

        then:
        result == expectedResult

        where:
        expectedResult         | value
        '21000000.00000000'    | BitcoinNetwork.MAX_MONEY
        '1.00000000'           | Coin.COIN
        '0.00100000'           | Coin.MILLICOIN
        '0.00000100'           | Coin.MICROCOIN
        '1E-8'                 | Coin.SATOSHI      // Actually, we probably want '0.00000001'
    }

    @Override
    void configureModule(module) {
        module.addSerializer(Coin.class, new CoinSerializer())
    }
}