package com.msgilligan.bitcoinj.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.core.Coin

/**
 * Convenience Category for coercing BigDecimal to Coin
 */
@CompileStatic
@Category(BigDecimal)
class BigDecimalCoinCategory {
    private static final BigDecimal satoshisPerBTCDecimal = new BigDecimal(Coin.COIN.value);

    def asType(Class target) {
        if (target==Coin) {
            BigDecimal satoshisDecimal = this.multiply(satoshisPerBTCDecimal);
            return Coin.valueOf(satoshisDecimal.longValueExact());
        }
    }
}
