package org.consensusj.bitcoinj.dsl.groovy.categories

import groovy.transform.CompileStatic
import org.bitcoinj.base.Coin

/**
 * Add convenience methods to Coin
 */
@CompileStatic
@Category(Coin)
class CoinCategory {
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
