package com.msgilligan.bitcoinj.groovy.categories

import org.bitcoinj.core.Coin
import spock.lang.Specification
import spock.util.mop.Use


/**
 * Spec to test NumberCategory coercion & conversion
 */
@Use(NumberCategory)
class NumberCategorySpec extends Specification {

    def "coercion operator works for integers"() {
        expect:
        (-1G as Coin).value == -1G * Coin.SATOSHI.value
        0G as Coin == Coin.ZERO
        1 as Coin == Coin.SATOSHI
        100 as Coin == Coin.MICROCOIN
        100_000_000G as Coin == Coin.COIN
        5_000_000_000G as Coin == Coin.FIFTY_COINS
    }

    def "coercion operator works for decimals"() {
        expect:
        0.0 as Coin == Coin.ZERO
        1.0 as Coin == Coin.SATOSHI
        100.0 as Coin == Coin.MICROCOIN
        5_000_000_000.0G as Coin == Coin.FIFTY_COINS
    }

    def "rounding errors are caught when converting BigDecimal" () {
        when:
        number as Coin

        then:
        ArithmeticException e = thrown()

        where:
        number << [ -0.1, 0.1, -0.00000001, 0.00000001, -0.000000000000001, 0.000000000000001]
    }

    /**
     * This is expected, though undesirable behavior
     */
    def "rounding errors are NOT caught when converting float" () {
        when:
        def coin = number as Coin

        then:
        coin == Coin.ZERO

        where:
        number << [ -0.1f, 0.1f, -0.00000001f, 0.00000001f, -0.000000000000001f, 0.000000000000001f]
    }

    def "basic test of .btc convenience method"() {
        expect:
        (-0.00000001).btc == Coin.NEGATIVE_SATOSHI
        0.0.btc == Coin.ZERO
        0.00000001.btc == Coin.SATOSHI
        0.000001.btc == Coin.MICROCOIN
        0.001.btc == Coin.MILLICOIN
        0.01.btc == Coin.CENT
        1.0.btc == Coin.COIN
        50.0.btc == Coin.FIFTY_COINS
        0G.btc == Coin.ZERO
        1G.btc == Coin.COIN
        50G.btc == Coin.FIFTY_COINS
    }

    def "test of .btc convenience method with floats"() {
        expect:
        0.0f.btc == Coin.ZERO
        1.0f.btc == Coin.COIN
        50.0f.btc == Coin.FIFTY_COINS
        0f.btc == Coin.ZERO
        1f.btc == Coin.COIN
        50f.btc == Coin.FIFTY_COINS
    }

    def ".btc convenience method rounds floats without throwing exception"() {
        expect:
        (-0.00000001f).btc == Coin.ZERO
        0.00000001f.btc == Coin.ZERO
        0.000001f.btc == Coin.ZERO
        0.001f.btc == Coin.ZERO
        0.01f.btc == Coin.ZERO
    }

    def "basic test of .satoshi convenience method"() {
        expect:
        (-1G).satoshi == Coin.NEGATIVE_SATOSHI
        0G.satoshi == Coin.ZERO
        1G.satoshi == Coin.SATOSHI
        100G.satoshi == Coin.MICROCOIN
        100_000G.satoshi == Coin.MILLICOIN
        1_000_000G.satoshi == Coin.CENT
        100_000_000G.satoshi == Coin.COIN
        5_000_000_000G.satoshi == Coin.FIFTY_COINS
    }

}