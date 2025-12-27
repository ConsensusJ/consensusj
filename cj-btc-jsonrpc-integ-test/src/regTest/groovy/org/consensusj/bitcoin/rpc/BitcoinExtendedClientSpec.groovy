package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.test.FundingSource
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification


/**
 * Basic tests of Extended Client
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class BitcoinExtendedClientSpec extends Specification {
    @Shared
    BitcoinExtendedClient client
    
    @Shared
    FundingSource funder

    def "There is well-known RegTestMiningAddress"() {
        expect:
        client.getRegTestMiningAddress() == BitcoinExtendedClient.DEFAULT_REGTEST_MINING_ADDRESS
    }

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
        // Not allowed on Descriptor Wallets
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