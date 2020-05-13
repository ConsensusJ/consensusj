package com.msgilligan.bitcoinj.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.core.Coin

import java.math.MathContext
import java.math.RoundingMode

/**
 * Add convenience methods to Coin
 */
@CompileStatic
@Category(Coin)
class CoinCategory {
    // Note: this duplicates code in json.conversion.BitcoinMath, but we don't want to depend on any other modules
    // Maybe basic conversion code needs to go to a standalone package or become part of `bitcoinj-core`.
    //
    private static final int DEFAULT_SCALE = Coin.SMALLEST_UNIT_EXPONENT;
    private static final BigDecimal bdSatoshiPerCoin = new BigDecimal(Coin.COIN.value);

    /**
     * Convert to BTC in BigDecimal format
     *
     * @return a BigDecimal object
     */
    public BigDecimal getDecimalBtc() {
        MathContext context = new MathContext(Coin.SMALLEST_UNIT_EXPONENT, RoundingMode.UNNECESSARY);
        BigDecimal satoshi = new BigDecimal(value, context)
        return satoshi.divide(bdSatoshiPerCoin, DEFAULT_SCALE, RoundingMode.UNNECESSARY)
    }

    // TODO: Needs more tests!

    Coin negative() {
        return valueOf(-this.value)
    }

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
