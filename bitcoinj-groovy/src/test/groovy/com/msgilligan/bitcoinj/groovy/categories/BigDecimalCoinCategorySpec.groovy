package com.msgilligan.bitcoinj.groovy.categories

import org.bitcoinj.core.Coin
import spock.lang.Specification
import spock.util.mop.Use


/**
 * Spec to test BigDecimalCoinCategory coercion
 */
@Use(BigDecimalCoinCategory)
class BigDecimalCoinCategorySpec extends Specification {
    def "basic test"() {
        expect:
        -0.00000001 as Coin == Coin.NEGATIVE_SATOSHI
        0.0 as Coin == Coin.ZERO
        0.00000001 as Coin == Coin.SATOSHI
        0.000001 as Coin == Coin.MICROCOIN
        0.001 as Coin == Coin.MILLICOIN
        0.01 as Coin == Coin.CENT
        1.0 as Coin == Coin.COIN
        50.0 as Coin == Coin.FIFTY_COINS
    }
}