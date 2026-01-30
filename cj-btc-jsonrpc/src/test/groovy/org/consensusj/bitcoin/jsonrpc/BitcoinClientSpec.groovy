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
