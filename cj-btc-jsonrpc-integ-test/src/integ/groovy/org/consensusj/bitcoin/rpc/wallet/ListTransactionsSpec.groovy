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
