package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.json.pojo.BlockInfo
import org.bitcoinj.core.Block
import org.bitcoinj.core.Sha256Hash

/**
 * Spec for getBlock() and getBlockInfo()
 */
class GetBlockSpec extends BaseRegTestSpec {

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

        when:
        def block = getBlock(blockCount)

        then:
        block instanceof Block

        when:
        def blockInfo = getBlockInfo(block.hash)

        then:
        blockInfo instanceof BlockInfo
        blockInfo.height == blockCount

        and:
        block.hash == blockInfo.hash
        block.nonce == blockInfo.nonce
        block.merkleRoot == blockInfo.merkleroot
        block.prevBlockHash == blockInfo.previousblockhash
    }
}