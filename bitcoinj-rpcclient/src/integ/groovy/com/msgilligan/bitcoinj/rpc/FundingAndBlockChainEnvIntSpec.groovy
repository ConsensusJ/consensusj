package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.test.RegTestEnvironment
import com.msgilligan.bitcoinj.test.RegTestFundingSource
import spock.lang.Specification


/**
 * Component-based test (no base test spec, required)
 */
class FundingAndBlockChainEnvIntSpec extends Specification {
    def "test it"() {
        given: "a client, a source of funds, and a blockchain environment"
        def client = new BitcoinExtendedClient(RpcURI.defaultRegTestURI, "bitcoinrpc", "pass")
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