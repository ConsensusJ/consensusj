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
