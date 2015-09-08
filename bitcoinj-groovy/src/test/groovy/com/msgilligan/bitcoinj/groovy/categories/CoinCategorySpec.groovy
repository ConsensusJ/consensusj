package com.msgilligan.bitcoinj.groovy.categories

import org.bitcoinj.core.Coin
import spock.lang.Specification
import spock.util.mop.Use

import static org.bitcoinj.core.NetworkParameters.MAX_MONEY

/**
 * Spec to test CoinCategory
 */
@Use(CoinCategory)
class CoinCategorySpec extends Specification {
    def "can convert to BTC (BigDecimal)"() {
        expect:
        Coin.NEGATIVE_SATOSHI.asBTC()   == -0.00000001
        Coin.SATOSHI.asBTC()            == 0.00000001
        Coin.MICROCOIN.asBTC()          == 0.000001
        Coin.MILLICOIN.asBTC()          == 0.001
        Coin.CENT.asBTC()               == 0.01
        Coin.COIN.asBTC()               == 1.0
        Coin.FIFTY_COINS.asBTC()        == 50.0
        MAX_MONEY.asBTC()               == 21_000_000.0
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
}