package org.consensusj.bitcoin.json.conversion

import org.bitcoinj.base.Address
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.BitcoinNetwork
import spock.lang.Unroll

/**
 * Test AddressDeserializer
 */
class AddressDeserializerSpec extends BaseObjectMapperSpec {
    @Unroll
    def "fragment #fragment scans to Coin #expectedResult"() {
        when:
        def result = mapper.readValue(fragment, Address.class)

        then:
        result == expectedResult

        where:
        fragment                                    | expectedResult
        '"mzoXt4rLjhcfkDPPo2rDXYMHzVnKDgmk2E"'      | AddressParser.getDefault(BitcoinNetwork.TESTNET).parseAddress('mzoXt4rLjhcfkDPPo2rDXYMHzVnKDgmk2E')
    }

    @Override
    void configureModule(module) {
        module.addDeserializer(Address.class, new AddressDeserializer())
    }
}