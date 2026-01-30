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
package org.consensusj.bitcoin.json.pojo

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Context
import org.bitcoinj.testing.FakeTxBuilder
import spock.lang.Specification

/**
 * RawTransactionInfo tests
 */
class RawTransactionInfoSpec extends Specification {
    def setupSpec() {
        Context.propagate(new Context());
    }
    
    def "Jackson-style constructor works"() {
        when:
        def raw = new RawTransactionInfo("FF",
                                Sha256Hash.ZERO_HASH,
                                1,
                                1,
                                null,
                                null,
                                Sha256Hash.ZERO_HASH,
                                0,
                                0,
                                0)

        then:
        raw != null
        raw.version == 1
    }

    def "Construct from Fake BitcoinJ transaction"() {
        given:
        def tx = FakeTxBuilder.createFakeTx(BitcoinNetwork.MAINNET)

        when:
        def raw = new RawTransactionInfo(tx)

        then:
        raw != null
    }
}
