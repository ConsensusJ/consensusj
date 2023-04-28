package org.consensusj.bitcoin.jsonrpc

import spock.lang.Specification

import static org.bitcoinj.base.BitcoinNetwork.REGTEST

/**
 * Smoke test
 */
class BitcoinExtendedClientSpec extends Specification {
    def "can construct and close"() {
        when:
        def client = new BitcoinExtendedClient(REGTEST, RpcURI.defaultRegTestURI, "", "")

        then:
        client != null

        when:
        client.close()

        then:
        client.getDefaultAsyncExecutor().isShutdown()
        client.getDefaultAsyncExecutor().isTerminated()
    }

}
