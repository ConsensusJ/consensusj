/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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