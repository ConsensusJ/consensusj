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
package org.consensusj.bitcoin.rpc.wallet

import org.consensusj.bitcoin.json.pojo.BitcoinTransactionInfo
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.IgnoreIf

/**
 * Basic tests of list transactions
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class ListTransactionsSpec extends BaseRegTestSpec {

    def "list transactions (no args)"() {
        when:
        List<BitcoinTransactionInfo> txs = client.listTransactions()

        then:
        txs.size() >= 0
        txs.size() <= 10
    }

    def "list all transactions (2 args)"() {
        when:
        List<BitcoinTransactionInfo> txs = client.listTransactions("*", Integer.MAX_VALUE)

        then:
        txs.size() >= 0
    }
}
