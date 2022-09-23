package org.consensusj.bitcoin.rpc.tx

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.groovy.test.JTransactionTestSupport
import org.bitcoinj.core.NetworkParameters
import spock.lang.Shared

/**
 * Base test class for testing bitcoinj transactions via P2P and RPC on RegTest
 * TODO: The inheritance of BitcoinClientDelegate through both BaseRegTestSpec and
 * JTransactionTestSupport seems to cause delegated calls (e.g. getNetParams()) to
 * get a NPE when looking for the client property.
 */
abstract class TxTestBaseSpec extends BaseRegTestSpec implements JTransactionTestSupport {
    @Shared
    NetworkParameters params

    void setupSpec() {
        params = client.getNetParams()
    }
}
