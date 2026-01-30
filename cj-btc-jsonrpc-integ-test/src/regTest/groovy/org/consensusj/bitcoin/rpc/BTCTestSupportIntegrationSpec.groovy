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
package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.base.Coin
import spock.lang.IgnoreIf

/**
 * Test Spec for BTCTestSupport.
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class BTCTestSupportIntegrationSpec extends BaseRegTestSpec {

    def "we can request newly-mined bitcoins"() {
        given:
        def requestingAddress = newAddress
        Coin requestedAmount = 1.btc

        when:
        requestBitcoin(requestingAddress, requestedAmount)

        and:
        generateBlocks(1)

        then:
        getBitcoinBalance(requestingAddress) == requestedAmount
    }
}