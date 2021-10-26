package org.consensusj.bitcoin.test.mainnet

import org.consensusj.bitcoin.test.BaseMainNetTestSpec
import org.consensusj.bitcoin.json.pojo.BlockInfo
import org.bitcoinj.core.Block
import org.bitcoinj.core.Sha256Hash

/**
 *
 */
class MainNetSmokeTest extends BaseMainNetTestSpec {
    def "return basic info" () {
        when: "we request info"
        def info = getNetworkInfo()

        then: "we get back some basic information"
        info != null
        info.version >= 90100
        info.protocolVersion >= 70002
    }

    def "get block info" () {
        when: "we request the hash"
        Sha256Hash hash = getBlockHash(324140)

        then: "we get the correct hash"
        hash.bytes == "00000000000000001e76250b3725547b5887329cfe3a8bb930a70e66747384d3".decodeHex()

        when: "we request blockinfo"
        BlockInfo blockInfo = getBlockInfo(hash)

        then: "we get the correct nonce"
        blockInfo.nonce == 3987703179

        when: "we request the binary block"
        Block block = getBlock(hash)

        then: "we get the correct nonce"
        block.nonce == 3987703179

    }

}