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
