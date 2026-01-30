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
package org.consensusj.bitcoin.services

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import spock.lang.Shared
import spock.lang.Specification

/**
 * Basic start/close test for WalletAppKitService
 */
class WalletAppKitServiceSpec extends Specification {
    @Shared
    WalletAppKitService appKitService;

    def setupSpec() {
        appKitService = WalletAppKitService.createTemporary(BitcoinNetwork.REGTEST, ScriptType.P2PKH, "cj-btc-services-unittest")
        appKitService.start()
    }

    def cleanupSpec() {
        appKitService.close()
    }

    def 'loaded correctly'() {
        expect:
        appKitService != null
        appKitService.network() == BitcoinNetwork.REGTEST
    }
}
