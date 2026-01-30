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

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification


/**
 * Component-based test (no base test spec, required)
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class FundingAndBlockChainEnvIntSpec extends Specification {
    @Shared BitcoinExtendedClient client

    void setupSpec () {
        client = BaseRegTestSpec.getClientInstance()  // Use a cached client for regtest mining reasons
    }
    
    def "RegTestEnvironment abstraction allows funding addresses and waiting for blocks"() {
        given: "a client, a source of funds, and a blockchain environment"
        def funder = new RegTestFundingSource(client)
        RegTestEnvironment chainEnv = new RegTestEnvironment(client)
        // Mine some blocks and setup a source of funds for testing
        def myAddress = funder.createFundedAddress(1.btc)
        def destAddress = client.getNewAddress()

        when: "we send coins"
        def txid = client.sendBitcoin(myAddress, destAddress, 0.5.btc)

        and: "a block is recorded"
        chainEnv.waitForBlocks(1)

        and: "we query the transaction"
        def tx = client.getTransaction(txid)

        and: "we check the balances"
        def sourceBalance = client.getBitcoinBalance(myAddress)
        def destBalance = client.getBitcoinBalance(destAddress)

        then:
        tx != null
        sourceBalance == 0.5.btc - client.stdTxFee
        destBalance == 0.5.btc
    }
}