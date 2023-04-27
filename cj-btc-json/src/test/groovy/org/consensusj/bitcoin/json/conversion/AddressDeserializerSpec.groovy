package org.consensusj.bitcoin.json.conversion

import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.DefaultAddressParser
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
        '"mzoXt4rLjhcfkDPPo2rDXYMHzVnKDgmk2E"'      | new DefaultAddressParser().parseAddress('mzoXt4rLjhcfkDPPo2rDXYMHzVnKDgmk2E', BitcoinNetwork.TESTNET)
    }

    @Override
    void configureModule(module) {
        module.addDeserializer(Address.class, new AddressDeserializer())
    }
}