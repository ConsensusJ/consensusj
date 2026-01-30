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
package org.consensusj.bitcoinj.dsl.groovy.categories

import org.bitcoinj.base.Coin
import spock.lang.Specification
import spock.util.mop.Use

import static org.bitcoinj.base.BitcoinNetwork.MAX_MONEY

/**
 * Spec to test CoinCategory
 */
@Use(CoinCategory)
class CoinCategorySpec extends Specification {
    def "can convert to BTC (BigDecimal)"() {
        expect:
        Coin.NEGATIVE_SATOSHI.toBtc()   == -0.00000001
        Coin.SATOSHI.toBtc()            == 0.00000001
        Coin.MICROCOIN.toBtc()          == 0.000001
        Coin.MILLICOIN.toBtc()          == 0.001
        Coin.CENT.toBtc()               == 0.01
        Coin.COIN.toBtc()               == 1.0
        Coin.FIFTY_COINS.toBtc()        == 50.0
        MAX_MONEY.toBtc()               == 21_000_000.0
    }

    // TODO: Many more tests!

    def "addition operator works"() {
        expect:
        Coin.COIN + Coin.COIN           == Coin.valueOf(2*Coin.COIN.longValue())
    }

    def "subtraction operator works"() {
        expect:
        Coin.COIN - Coin.COIN           == Coin.ZERO
    }

    def "multiplication operator works"() {
        expect:
        Coin.COIN * 2                   == Coin.valueOf(2*Coin.COIN.longValue())
    }

    def "division operator works with Number"() {
        expect:
        (Coin.COIN * 2) / 2             == Coin.COIN
    }

    def "division operator works with Coin"() {
        expect:
        (Coin.COIN * 2) / Coin.COIN     == 2L
    }

    def "negation works" () {
        expect:
        -Coin.COIN == Coin.COIN * -1
    }
}