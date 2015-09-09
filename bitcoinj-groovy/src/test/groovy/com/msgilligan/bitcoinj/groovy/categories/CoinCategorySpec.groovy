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
        Coin.NEGATIVE_SATOSHI.decimalBtc   == -0.00000001
        Coin.SATOSHI.decimalBtc            == 0.00000001
        Coin.MICROCOIN.decimalBtc          == 0.000001
        Coin.MILLICOIN.decimalBtc          == 0.001
        Coin.CENT.decimalBtc               == 0.01
        Coin.COIN.decimalBtc               == 1.0
        Coin.FIFTY_COINS.decimalBtc        == 50.0
        MAX_MONEY.decimalBtc               == 21_000_000.0
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