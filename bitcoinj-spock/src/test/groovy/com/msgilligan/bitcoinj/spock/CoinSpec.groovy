package com.msgilligan.bitcoinj.spock

import org.bitcoinj.core.Coin
import spock.lang.Specification


/**
 * Basic tests/demo of bitcoinj Coin class.
 */
class CoinSpec extends Specification {

    def "valueOf can make ZERO correctly"() {
        expect:
        Coin.valueOf(0,0) == Coin.ZERO
    }

    def "valueOf can make one bitcoin correctly"() {
        expect:
        Coin.valueOf(1,0) == Coin.COIN
    }

    def "valueOf can make one bitcent correctly"() {
        expect:
        Coin.valueOf(0,1) == Coin.CENT
    }
}