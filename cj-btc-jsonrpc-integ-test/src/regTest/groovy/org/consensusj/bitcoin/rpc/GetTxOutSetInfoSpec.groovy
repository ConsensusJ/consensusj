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
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.base.Coin
import spock.lang.IgnoreIf

/**
 * Test Specification for getTxOutSetInfo
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
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
