package com.msgilligan.bitcoinj.rpc.blockchain

import com.msgilligan.bitcoinj.BaseRegTestSpec

/**
 * Functional test of `gettxoutsetinfo` via {@link BitcoinClient#getTxOutSetInfo}
 * See:
 * https://blog.okcoin.com/2020/05/12/btc-developer-asks-where-are-the-coins/
 * https://github.com/bitcoin/bitcoin/pull/18000
 * https://bitcoin.stackexchange.com/a/38998
 * https://bitcoin.stackexchange.com/a/24684
 */
class GetTxOutSetInfoSpec extends BaseRegTestSpec
{
    def "response fields are present and pass minimal consistency checks "() {
        given: "a certain starting height"
        def startHeight = blockCount

        when: "we call the RPC"
        def txOutSetInfo = getTxOutSetInfo()

        then: "The result passes basic sanity checks"
        txOutSetInfo.height <= startHeight
        txOutSetInfo.bestBlock != null
        txOutSetInfo.transactions > 0
        txOutSetInfo.txOuts >= txOutSetInfo.transactions
        txOutSetInfo.bogoSize > txOutSetInfo.txOuts * 50
        txOutSetInfo.hashSerialized2 != null
        txOutSetInfo.diskSize >= 0
        txOutSetInfo.totalAmount > 0 && txOutSetInfo.totalAmount <= startHeight * 50
    }
}
