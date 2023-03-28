package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.base.Coin
import org.bitcoinj.base.LegacyAddress
import org.bitcoinj.base.Sha256Hash

class BitcoinSpec extends BaseRegTestSpec {
    static final Coin testAmount = 2.btc

    def "get network info" () {
        when: "we request network info"
        def netInfo = client.getNetworkInfo()

        then: "we get back some basic information"
        netInfo != null
        netInfo.version >= 180000
        netInfo.protocolVersion >= 70002
    }

    def "get blockchain info" () {
        when: "we request blockchain info"
        def chainInfo = client.getBlockChainInfo()

        then: "we get back some basic information"
        chainInfo != null
        chainInfo.chain == "regtest"
        chainInfo.blocks >= 1
        chainInfo.headers >= 1
        chainInfo.bestBlockHash != null
    }

    def "Get a list of available commands"() {
        given:
        def commands = client.getCommands()

        expect:
        commands != null
        commands.contains('getblockchaininfo')
        commands.contains('getnetworkinfo')
        commands.contains('help')
        commands.contains('stop')
    }

    def "Use RegTest mode to generate a block upon request"() {
        given: "a certain starting height"
        def startHeight = blockCount
        def version10 = client.getNetworkInfo().version > 100000

        when: "we generate 1 new block"
        def result = generateBlocks(1)

        then: "the block height is 1 higher"
        blockCount == startHeight + 1

        and: "We have a txid if version > 10"
        !version10 || version10 && result.size() == 1 && result[0] instanceof Sha256Hash
    }

    def "When we send an amount to a newly created address, it arrives"() {
        given: "A new, empty Bitcoin address"
        def destinationAddress = getNewAddress()

        when: "we send it testAmount (from coins mined in RegTest mode)"
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")

        and: "we generate 1 new block"
        generateBlocks(1)

        then: "the new address has a balance of testAmount"
        testAmount == getReceivedByAddress(destinationAddress)
        // TODO: check balance of source address/wallet
    }

    def "Get a list of unspent transaction outputs"() {
        when: "we request unspent transaction outputs"
        def unspent = listUnspent()

        then: "there is at least 1"
        unspent.size() >= 1
    }

    def "Get a filtered list of unconfirmed transaction outputs"() {
        when: "we create a new address and send #testAmount to it"
        def destinationAddress = getNewAddress()
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")

        and: "we request unconfirmed unspent outputs for #destinationAddress"
        def unspent = listUnspent(0, 0, [destinationAddress])

        then: "there is at least 1"
        unspent.size() >= 1

        and: "they have 0 confirmations"
        unspent.every { output -> output.confirmations == 0 }

        and: "they are associated with #destinationAddress"
        unspent.every { output -> output.address == destinationAddress }
    }

    def "We can get the correct private key for an address"() {
        when: "we create a new address and dump it's private key"
        def address = getNewAddress()
        def netParams = getNetParams()
        def key = dumpPrivKey(address)

        then: "when we convert the dumped key to an address we get the same address"
        LegacyAddress.fromKey(netParams, key) == address
    }

    def "We can get information about chain tips"() {
        when:
        def tips = getChainTips()

        then:
        tips != null
        tips.size() >= 1

        when:
        def activeTip = tips.find {it.status == "active"}

        then:
        activeTip != null
        activeTip.branchlen == 0
        activeTip.hash instanceof Sha256Hash
        activeTip.height > 0
        activeTip.status == "active"
    }

    def "We can get a list of address groupings"() {
        when:
        def groupings = listAddressGroupings()

        then:
        groupings.size() > 1
    }
}
