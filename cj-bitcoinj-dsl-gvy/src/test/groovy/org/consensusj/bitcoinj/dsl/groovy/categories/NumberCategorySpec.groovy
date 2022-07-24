package org.consensusj.bitcoinj.dsl.groovy.categories

import org.bitcoinj.core.Coin
import org.consensusj.bitcoinj.dsl.groovy.categories.NumberCategory
import spock.lang.Specification
import spock.util.mop.Use


/**
 * Spec to test NumberCategory coercion & conversion
 */
@Use(NumberCategory)
class NumberCategorySpec extends Specification {

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

    /**
     * This is expected, though undesirable behavior
     */
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

    def "we can compare btc, satoshi, and Coin"() {
        expect:
        1.btc == 100_000_000.satoshi
        100.satoshi == Coin.MICROCOIN
    }

    def "doesn't break groovy SDK"() {
        expect:
        0 as Byte == 0.byteValue()
    }
}