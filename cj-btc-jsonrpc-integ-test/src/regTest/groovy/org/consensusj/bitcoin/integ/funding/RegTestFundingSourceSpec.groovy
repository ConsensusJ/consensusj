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
package org.consensusj.bitcoin.integ.funding


import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import spock.lang.IgnoreIf
import spock.lang.Shared

/**
 * Test the fundingSource created by BaseRegTestSpec
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class RegTestFundingSourceSpec extends BaseRegTestSpec {
    @Shared
    RegTestFundingSource source;

    def "smoke"() {
        expect:
        source != null
    }

    def "we can consolidate coins"() {
        when:
        source.consolidateCoins()

        then:
        noExceptionThrown()
    }

    def "we can fund an address"() {
        given:
        Address requestor = client.getNewAddress()

        when:
        Sha256Hash txid = source.requestBitcoin(requestor, Coin.CENT)
        def info = client.getTransaction(txid)
        
        then:
        info.confirmations == 0

        when:
        client.generateBlocks(1)
        info = client.getTransaction(txid)
        def balance = client.getBitcoinBalance(requestor)

        then:
        info.confirmations == 1
        balance == Coin.CENT
    }

    def setupSpec() {
        // Load our own variable so we can cast it to the right type
        source = (RegTestFundingSource) fundingSource;
    }

}
