package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
import com.msgilligan.bitcoinj.json.pojo.TxOutSetInfo
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Coin

/**
 * Test Specification for getTxOutSetInfo
 */
class GetTxOutSetInfoSpec extends BaseRegTestSpec {
    def "getTxOutSetInfo passes smoke test"() {
        when:
        TxOutSetInfo info = client.getTxOutSetInfo()

        then:
        info.height > 0
        info.bogoSize > 0
        info.diskSize > 0
        info.transactions > 0
        info.txOuts > 0
        info.bestBlock instanceof Sha256Hash
        info.hashSerialized2 instanceof Sha256Hash
        info.totalAmount >= Coin.ZERO
        info.totalAmount <= Coin.COIN * 21_000_000
    }
}
