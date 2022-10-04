package org.consensusj.bitcoin.rpc.tx

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.groovy.test.JTransactionTestSupport
import org.bitcoinj.core.NetworkParameters
import spock.lang.Shared

/**
 * Base test class for testing bitcoinj transactions via P2P and RPC on RegTest
 */
abstract class TxTestBaseSpec extends BaseRegTestSpec implements JTransactionTestSupport {
    @Shared
    NetworkParameters params

    void setupSpec() {
        params = client.getNetParams()
    }
}
