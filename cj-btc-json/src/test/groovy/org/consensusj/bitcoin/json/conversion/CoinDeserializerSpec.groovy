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

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
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
        '21000000.0'    | BitcoinNetwork.MAX_MONEY
        '1.0'           | Coin.COIN
        '0.001'         | Coin.MILLICOIN
        '0.000001'      | Coin.MICROCOIN
        '0.00000001'    | Coin.SATOSHI
        '"21000000.0"'  | BitcoinNetwork.MAX_MONEY
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