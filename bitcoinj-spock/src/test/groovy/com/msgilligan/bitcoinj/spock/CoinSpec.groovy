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

    def "addition"() {
        expect:
        Coin.COIN.add(Coin.COIN)           == Coin.valueOf(2*Coin.COIN.longValue())
    }

    def "subtraction"() {
        expect:
        Coin.COIN.subtract(Coin.COIN)      == Coin.ZERO
    }

    def "multiplication"() {
        expect:
        Coin.COIN.multiply(2)           == Coin.valueOf(2*Coin.COIN.longValue())
    }

    def "divide by Number"() {
        expect:
        Coin.FIFTY_COINS.divide(50)     == Coin.COIN
    }

    def "divide by Number with remainder"() {
        when:
        def result = Coin.COIN.divideAndRemainder(2)

        then:
        result[0] == Coin.valueOf(50_000_000)
        result[1] == Coin.ZERO
    }

    def "divide by Coin"() {
        expect:
        Coin.FIFTY_COINS.divide(Coin.FIFTY_COINS)   == 1L
        Coin.FIFTY_COINS.divide(Coin.COIN)          == 50L
    }

    // This is the only arithmetic method of Coin that happens
    // to match Groovy operator overloading. Use CoinCategory if
    // you need operator overloading for other operations.
    def "multiplication operator works"() {
        expect:
        Coin.COIN * 2                   == Coin.valueOf(2*Coin.COIN.longValue())
    }


}