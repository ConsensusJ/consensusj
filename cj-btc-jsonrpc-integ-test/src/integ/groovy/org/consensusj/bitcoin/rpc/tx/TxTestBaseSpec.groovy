package org.consensusj.bitcoin.rpc.tx

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.jsonrpc.groovy.test.JTransactionTestSupport

/**
 * Base test class for testing bitcoinj transactions via P2P and RPC on RegTest
 */
abstract class TxTestBaseSpec extends BaseRegTestSpec implements JTransactionTestSupport {
}
