package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
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
        generate()

        then: "we have the correct amount of BTC there, or possibly more due to block reward"
        getBitcoinBalance(throwAwayAddress) >= 25.btc
    }

    def "Be able to fund wealthy account from mining profits"() {
        when: "we send some BTC to an address"
        wealthyAddress = getNewAddress(testAccount1Name)
        sendToAddress(wealthyAddress, sendAmount*2 + extraAmount)
        generate()

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
        generate()

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
        found.account == testAccount1Name
        found.address == wealthyAddress
    }

}