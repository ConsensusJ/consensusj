package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.base.Coin

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
        info.diskSize >= 0
        info.transactions > 0
        info.txOuts > 0
        info.bestBlock instanceof Sha256Hash
        info.hashSerialized3 instanceof Sha256Hash
        info.totalAmount >= Coin.ZERO
        info.totalAmount <= Coin.COIN * 21_000_000
    }
}
