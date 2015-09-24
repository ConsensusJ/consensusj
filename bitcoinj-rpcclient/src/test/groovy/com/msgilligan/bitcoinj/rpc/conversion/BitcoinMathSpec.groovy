package com.msgilligan.bitcoinj.rpc.conversion

import com.msgilligan.bitcoinj.json.conversion.BitcoinMath
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import spock.lang.Specification
import spock.lang.Unroll


/**
 * Unit tests for BitcoinMathSpec
 */
class BitcoinMathSpec extends Specification {

    @Unroll
    def "we can convert #btc btc to #satoshi satoshi"(BigDecimal btc, long satoshi) {
        when:
        def result = BitcoinMath.btcToSatoshi(btc)

        then:
        result == satoshi

        where:
        btc         | satoshi
        21_000_000  | NetworkParameters.MAX_MONEY.value
        10.0        |  10*Coin.COIN.value
         0.00000001 | 1
    }

    @Unroll
    def "we can convert #satoshi satoshi to #btc btc"(long satoshi, BigDecimal btc) {
        when:
        def result = BitcoinMath.satoshiToBtc(satoshi)

        then:
        result == btc

        where:
        satoshi     | btc
        100_000_000 | 1
        298         | 0.00000298
        1           | 0.00000001
        NetworkParameters.MAX_MONEY.value |  21_000_000
    }

}