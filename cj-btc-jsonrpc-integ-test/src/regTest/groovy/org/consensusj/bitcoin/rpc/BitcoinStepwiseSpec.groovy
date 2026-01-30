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
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class BitcoinStepwiseSpec extends BaseRegTestSpec {
    final static Coin sendAmount = 10.btc
    final static Coin extraAmount = 0.1.btc
    final static String testAccount1Name = "BitcoinStepwiseSpec1"
    final static String testAccount2Name = "BitcoinStepwiseSpec2"

    @Shared
    Address wealthyAddress

    def "Send some funds to an address (that may also get block reward)"() {
        when: "we send some BTC to a newly created address"
        def throwAwayAddress = getNewAddress()
        sendToAddress(throwAwayAddress, 25.btc)
        generateBlocks(1)

        then: "we have the correct amount of BTC there, or possibly more due to block reward"
        getBitcoinBalance(throwAwayAddress) >= 25.btc
    }

    def "Be able to fund wealthy account from mining profits"() {
        when: "we send some BTC to an address"
        wealthyAddress = getNewAddress(testAccount1Name)
        sendToAddress(wealthyAddress, sendAmount*2 + extraAmount)
        generateBlocks(1)

        then: "we have the correct amount of BTC there"
        getBitcoinBalance(wealthyAddress) == sendAmount*2 + extraAmount
    }

    def "Send an amount to a newly created address"() {
        setup: "initial balance"
        Coin wealthyStartBalance = getBitcoinBalance(wealthyAddress)
        Coin testAmount = 1.btc

        when: "we create a new address and send testAmount to it"
        Address destinationAddress = getNewAddress(testAccount2Name)
        sendBitcoin(wealthyAddress, destinationAddress, testAmount)
        generateBlocks(1)

        then: "the new address has a balance of testAmount"
        getBitcoinBalance(destinationAddress) == testAmount

        and: "the source address is poorer by the correct amount"
        getBitcoinBalance(wealthyAddress) == wealthyStartBalance - testAmount - stdTxFee
    }

    def "wealthyAddress shows up in listreceivedbyaddress"() {
        when:
        def result = listReceivedByAddress(1, false)
        def found = result.find { it.address == wealthyAddress }

        then:
        found != null
        found.label == testAccount1Name
        found.address == wealthyAddress
    }

}