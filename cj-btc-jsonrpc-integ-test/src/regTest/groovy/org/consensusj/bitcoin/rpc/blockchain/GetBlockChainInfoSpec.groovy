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
package org.consensusj.bitcoin.rpc.blockchain

import org.bitcoinj.base.Sha256Hash
import org.consensusj.bitcoin.json.pojo.BlockChainInfo
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.IgnoreIf

/**
 * Functional test of `gettxoutsetinfo` via {@link BitcoinClient#getBlockChainInfo}
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class GetBlockChainInfoSpec extends BaseRegTestSpec {
    def "response fields are present and pass minimal consistency checks "() {
        when: "we call the RPC"
        BlockChainInfo blockChainInfo = getBlockChainInfo()

        then: "The result passes basic sanity checks"
        blockChainInfo.chain == "regtest"
        blockChainInfo.blocks >= 0
        blockChainInfo.headers >= 0
        blockChainInfo.bestBlockHash instanceof Sha256Hash
        blockChainInfo.difficulty > 0
        blockChainInfo.verificationProgress > 0
        blockChainInfo.chainWork.length == 48
    }
}
