package org.consensusj.bitcoin.jsonrpc

import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static org.bitcoinj.base.BitcoinNetwork.REGTEST

/**
 * A few basic tests of BitcoinClient that don't need a server/network
 */
class BitcoinClientSpec extends Specification {
    def "can construct and close"() {
        when:
        def client = new BitcoinClient(REGTEST, RpcURI.defaultRegTestURI, "", "")

        then:
        client != null

        when:
        client.close()

        then:
        client.getDefaultAsyncExecutor().isShutdown()
        client.getDefaultAsyncExecutor().isTerminated()
    }

    def "can construct and close twice"() {
        when:
        def client = new BitcoinClient(REGTEST, RpcURI.defaultRegTestURI, "", "")

        then:
        client != null

        when:
        client.close()
        client.close()

        then:
        client.getDefaultAsyncExecutor().isShutdown()
        client.getDefaultAsyncExecutor().isTerminated()
    }

    def "can construct and auto-close"() {
        when:
        def client
        try ( def c = new BitcoinClient(REGTEST, RpcURI.defaultRegTestURI, "", "") ) {
            client = c;
        }

        then:
        client != null
        client.getDefaultAsyncExecutor().isShutdown()
        client.getDefaultAsyncExecutor().isTerminated()
    }

    def "can construct and auto-close after creating a thread"() {
        when:
        def client
        def result;
        try ( def c = new BitcoinClient(REGTEST, RpcURI.defaultRegTestURI, "", "") ) {
            client = c
            // Run a simple function using the BitcoinClients thread pool
            CompletableFuture<Integer> cf = c.supplyAsync(() -> 5)
            result = cf.join()
        }

        then:
        client != null
        result == 5
        client.getDefaultAsyncExecutor().isShutdown()
        client.getDefaultAsyncExecutor().isTerminated()
    }

}
