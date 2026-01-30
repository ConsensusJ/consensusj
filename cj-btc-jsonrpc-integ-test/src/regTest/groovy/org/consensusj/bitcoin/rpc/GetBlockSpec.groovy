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
import org.consensusj.bitcoin.json.pojo.BlockInfo
import org.bitcoinj.core.Block
import org.bitcoinj.base.Sha256Hash
import spock.lang.IgnoreIf

/**
 * Spec for getBlock() and getBlockInfo()
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
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
        !version10 || version10 && result.size() == 1 && (result[0] instanceof Sha256Hash)

        when:
        def block = getBlock(blockCount)

        then:
        (block instanceof Block)

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