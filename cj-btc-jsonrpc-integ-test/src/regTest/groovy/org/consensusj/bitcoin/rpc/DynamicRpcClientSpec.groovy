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

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.TestServers
import org.bitcoinj.base.Address
import org.bitcoinj.crypto.ECKey
import org.consensusj.jsonrpc.groovy.DynamicRpcClient
import org.consensusj.jsonrpc.JsonRpcStatusException
import spock.lang.Shared
import spock.lang.Specification


/**
 * Test DynamicRPCClient against a Bitcoin RPC server in RegTest mode
 *
 */
class DynamicRpcClientSpec extends Specification {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword

    @Shared
    DynamicRpcClient client

    void setupSpec() {
        client = new DynamicRpcClient(RpcURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)

// TODO: Need to implement waitForServer()
// waitForServer() is in BitcoinClient because it uses getBlockCount()
// Either implement something that uses a non-existent method and wait for a "invalid method" response
// to indicate server is up or create a Base BitcoinRPC that has waitForServer() but not static RPC methods

//        log.info "Waiting for server..."
//        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
//        if (!available) {
//            log.error "Timeout error."
//        }
//        assert available
    }

    def "getblockcount"() {
        when:
        def result = client.getblockcount()

        then:
        result >= 0
    }

    def "generatetoaddress"() {
        given:
        Address toAddress = new ECKey().toAddress(ScriptType.P2PKH, BitcoinNetwork.REGTEST)

        when:
        def result = client.generatetoaddress(2, toAddress.toString())

        then:
        result != null /* Bitcoin 0.10.x or later */
    }

    def "getnetworkinfo" () {
        when:
        def info = client.getnetworkinfo()

        then:
        info != null
        info.version >= 90100
        info.protocolversion >= 70002

    }

    def "non-existent method throws JsonRPCStatusException"() {
        when:
        client.idontexist("parm", 2)

        then:
        JsonRpcStatusException e = thrown()
        e.message == "Method not found"
        //e.httpMessage == "Not Found"          // Java.net.http, HTTP/2, and HTTP/3 don't provide error reason text
        e.httpCode == 200 || e.httpCode == 404  // Newer (e.g. v29.0) bitcoind returns 200, older returns 404
        e.jsonRpcCode == -32601
        e.response == null
        e.responseJson.result == null
        e.responseJson.error.code == -32601
        e.responseJson.error.message == "Method not found"
    }

}