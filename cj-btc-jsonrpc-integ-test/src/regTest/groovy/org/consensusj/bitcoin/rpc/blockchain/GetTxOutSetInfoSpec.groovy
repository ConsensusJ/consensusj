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

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.base.Coin
import spock.lang.IgnoreIf

/**
 * Functional test of `gettxoutsetinfo` via {@link BitcoinClient#getTxOutSetInfo}
 * See:
 * https://blog.okcoin.com/2020/05/12/btc-developer-asks-where-are-the-coins/
 * https://github.com/bitcoin/bitcoin/pull/18000
 * https://bitcoin.stackexchange.com/a/38998
 * https://bitcoin.stackexchange.com/a/24684
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
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
        txOutSetInfo.hashSerialized3 != null
        txOutSetInfo.diskSize >= 0
        txOutSetInfo.totalAmount > Coin.ZERO && txOutSetInfo.totalAmount <= Coin.valueOf(startHeight * 50, 0)
    }
}
