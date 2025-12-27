package org.consensusj.bitcoin.alpha

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.core.NetworkParameters
import org.consensusj.jsonrpc.groovy.DynamicRpcClient
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.TestServers
import org.bitcoinj.base.Address
import org.bitcoinj.crypto.DumpedPrivateKey
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * A few integration tests for Sidechain Elements Alpha `alphad` running in RegTest mode
 * Use DynamicRPCClient because we need weak typing since an Alpha address isn't a bitcoinj Address
 */
@Ignore("Written to work with alphad -- not part of any current CI job")
@Stepwise
class AlphaSpec extends Specification {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    static final testAmount = 2.0
    static final netParams = NetworkParameters.of(BitcoinNetwork.REGTEST)

    @Shared DynamicRpcClient client

    @Shared def fundedAddress
    @Shared String txid
    @Shared ECKey key

    def "When we send an amount to a newly created address, it arrives"() {
        given: "A new, empty Bitcoin address"
        def confidentialAddress = client.getnewaddress()
        fundedAddress = client.validateaddress(confidentialAddress).unconfidential

        when: "we send it testAmount (from coins mined in RegTest mode)"
        txid = client.sendtoaddress(fundedAddress, testAmount, "comment", "comment-to")

        and: "we generate 1 new block"
        client.setgenerate(true, 1)

        then: "the new address has a balance of testAmount"
        testAmount == client.getreceivedbyaddress(fundedAddress)
        def keyString = client.dumpprivkey(fundedAddress)

        when:
        key = DumpedPrivateKey.fromBase58(null, keyString).getKey()
        // TODO: check balance of source address/wallet

        then:
        key != null
    }

    def "find tx from txid"() {
        when: ""
        def tx = client.getrawtransaction(txid, 0)

        and:
        def decoded = client.decoderawtransaction(tx)

        then:
        decoded != null
    }

    def "create, sign, and send a raw transaction"() {
        when: ""
        def result = client.settxfee(0)
        def destinationAddress = client.getnewaddress()
        def destOldAddress = client.validateaddress(destinationAddress).unconfidential
        def transactions = [[txid: txid, vout: 0, nValue: testAmount]]
        def addresses = [(destOldAddress): testAmount]

        and: ""
        def rawtx = client.createrawtransaction(transactions, addresses)

        and: ""
        def signResult = client.signrawtransaction(rawtx)

        then: ""
        signResult.hex != null
        signResult.complete == true

        when:
        def decoded = client.decoderawtransaction(signResult.hex)

        and:
        def tx2id = client.sendrawtransaction(signResult.hex)

        then:
        tx2id != null

        when: "we try to read a tx into bitcoinj"
        def confrawtx = client.getrawtransaction(tx2id)
        // The following line fails because the transaction doesn't match
        //def jTx = new Transaction(RegTestParams.get(), confrawtx.decodeHex())

        then:
        confrawtx != null
        //jTx.inputs.size() > 0
        //jTx.outputs.size() > 0
    }

    @Ignore
    def "create a Tx with Bitcoinj and send it"() {
        when:
        def result = client.settxfee(0)
        def destinationConfidentialAddress = client.getnewaddress()
        def destinationAddress = client.validateaddress(destinationConfidentialAddress).unconfidential
        Transaction tx = new Transaction()
        tx.addOutput(testAmount.btc, Address.fromBase58(netParams, destinationAddress))
        TransactionOutPoint utxo = new TransactionOutPoint(netParams, 0, Sha256Hash.of(txid.decodeHex()))
//        tx.addSignedInput(utxo, ScriptBuilder.createOutputScript(Address.fromBase58(netParams, fundedAddress)), key)
        tx.addInput(utxo.hash(), utxo.index(), ScriptBuilder.createOutputScript(Address.fromBase58(netParams, fundedAddress)))
        def signed = client.signrawtransaction(tx.serialize().encodeHex().toString())
        def tx2id = client.sendrawtransaction(signed.hex)

        then:
        txid != null
    }


    void setupSpec() {
        client = new DynamicRpcClient(RpcURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)
    }
}