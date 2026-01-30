/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoinj.dsl.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.base.Coin


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
        return Coin.ofSat(btcToSatoshi(self))
    }

    private static Coin satoshiAsCoin(Number self) {
        return Coin.ofSat(asSatoshi(self))
    }

    private static long btcToSatoshi(Number self) {
        switch(self) {
            case BigDecimal:    return Coin.btcToSatoshi((BigDecimal) self)
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
