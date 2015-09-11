package com.msgilligan.bitcoinj.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.core.Coin


/**
 * Convenience Category for converting Numbers to the Coin class
 *
 */
@CompileStatic
@Category(Number)
class NumberCategory {
    private static final BigDecimal satoshiPerBTCDecimal = new BigDecimal(Coin.COIN.value)
    private static final BigInteger satoshiPerBTCBigInt = BigInteger.valueOf(Coin.COIN.value)

    /**
     * Treat number as an amount in Satoshi and return a Coin
     *
     * @return a Coin object
     */
    public Coin getSatoshi() {
        return satoshiAsCoin(this)
    }

    /**
     * Treat number as an amount in BTC and return a Coin
     *
     * @return a Coin object
     */
    public Coin getBtc() {
        return btcAsCoin(this)
    }

    private static Coin btcAsCoin(Number self) {
        return Coin.valueOf(btcToSatoshi(self))
    }

    private static Coin satoshiAsCoin(Number self) {
        return Coin.valueOf(asSatoshi(self))
    }

    private static long btcToSatoshi(Number self) {
        switch(self) {
            case BigDecimal:    return ((BigDecimal) self).multiply(satoshiPerBTCDecimal).longValueExact()
            case BigInteger:    return ((BigInteger) self).multiply(satoshiPerBTCBigInt).longValue()
            default:            return self.longValue() *  Coin.COIN.value
        }
    }

    private static long asSatoshi(Number self) {
        switch(self) {
            case BigDecimal:    return ((BigDecimal) self).longValueExact()
            case BigInteger:    return ((BigInteger) self).longValue()
            default:            return self.longValue()
        }
    }
}
