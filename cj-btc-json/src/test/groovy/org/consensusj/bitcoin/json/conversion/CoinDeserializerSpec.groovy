package org.consensusj.bitcoin.json.conversion

import org.bitcoinj.base.Coin
import org.bitcoinj.core.NetworkParameters
import spock.lang.Unroll

/**
 * Spock Spec to test CoinDeserializer
 */
class CoinDeserializerSpec extends BaseObjectMapperSpec {
    @Unroll
    def "fragment #fragment scans to Coin #expectedResult"() {
        when:
        def result = mapper.readValue(fragment, Coin.class)

        then:
        result == expectedResult

        where:
        fragment        | expectedResult
        '21000000.0'    | NetworkParameters.MAX_MONEY
        '1.0'           | Coin.COIN
        '0.001'         | Coin.MILLICOIN
        '0.000001'      | Coin.MICROCOIN
        '0.00000001'    | Coin.SATOSHI
        '"21000000.0"'  | NetworkParameters.MAX_MONEY
        '"1.0"'         | Coin.COIN
        '"0.001"'       | Coin.MILLICOIN
        '"0.000001"'    | Coin.MICROCOIN
        '"0.00000001"'  | Coin.SATOSHI
    }

    @Override
    void configureModule(module) {
        module.addDeserializer(Coin.class, new CoinDeserializer())
    }
}