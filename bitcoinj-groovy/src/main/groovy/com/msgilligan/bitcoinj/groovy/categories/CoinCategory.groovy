package com.msgilligan.bitcoinj.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.core.Coin

/**
 * Add convenience methods to Coin
 */
@CompileStatic
@Category(Coin)
class CoinCategory {
    private static final BigDecimal bdSatoshiPerCoin = new BigDecimal(Coin.COIN.longValue());
    /**
     * Convert to BTC in BigDecimal format
     *
     * @return a BigDecimal object
     */
    public BigDecimal getDecimalBtc() {
        BigDecimal satoshi = new BigDecimal(value)
        //TODO: Add rounding mode?
        return satoshi.divide(bdSatoshiPerCoin)
    }

    // TODO: Needs more tests!

    Coin plus(Coin right) {
        return this.add(right)
    }

    Coin minus(Coin right) {
        return this.subtract(right)
    }

    // Multiply not needed, Coin and Groovy use same name 'multiply'

    Coin div(Long right) {
        return this.divide(right.longValue());
    }

    Long div(Coin right) {
        return this.divide(right);
    }
}
