package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
import com.msgilligan.bitcoinj.test.FundingSource
import com.msgilligan.bitcoinj.test.RegTestEnvironment
import com.msgilligan.bitcoinj.test.RegTestFundingSource
import spock.lang.Shared
import spock.lang.Specification


/**
 * Basic tests of Extended Client
 */
class BitcoinExtendedClientSpec extends Specification {
    @Shared
    BitcoinExtendedClient client
    
    @Shared
    FundingSource funder

    def "Can create a funded address and send coins with sendBitcoin()"() {
        given: "a client, a source of funds, and a blockchain environment"
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
        tx.amount == 0.btc  // inputs and outputs balance out as all are in the same wallet
        tx.fee == -10000.satoshi
        tx.details.findAll{ it.address == myAddress}.size() == 2
        tx.details.findAll{ it.address == destAddress}.size() == 2
        tx.details.findAll{ it.address == myAddress}.sum{it.amount} == 0.btc
        tx.details.findAll{ it.category == "send"}.sum{it.amount} == -(50000000.satoshi + 49990000.satoshi)
        tx.details.findAll{ it.category == "receive"}.sum{it.amount} == 50000000.satoshi + 49990000.satoshi
        tx.details.findAll().sum{it.amount.value} == 0
        tx.details.findAll{ it.address == destAddress}.sum{it.amount.value} == 0
        tx.confirmations >= 1
        sourceBalance == 0.5.btc - client.stdTxFee
        destBalance == 0.5.btc
    }

    def "Can create a funded address and sign a transaction locally using createSignedTransaction()"() {
        given:
        def fundingAddress = funder.createFundedAddress(10.btc)
        def key = client.dumpPrivKey(fundingAddress)
        def destinationAddress = client.getNewAddress("destinationAddress")

        when: "we create an signed bitcoinj transaction, spending from fundingAddress to destinationAddress"
        def tx = client.createSignedTransaction(key, destinationAddress, 1.0.btc)

        then: "there should be a valid signed transaction"
        tx != null
        tx.outputs.size() > 0
        tx.inputs.size() > 0
    }

    void setup() {
        client = BaseRegTestSpec.getClientInstance()
        funder = new RegTestFundingSource(client)
    }
}