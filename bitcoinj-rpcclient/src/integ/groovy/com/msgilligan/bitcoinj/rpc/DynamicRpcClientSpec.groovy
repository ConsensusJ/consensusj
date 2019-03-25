package com.msgilligan.bitcoinj.rpc

import com.fasterxml.jackson.databind.node.NullNode
import com.msgilligan.bitcoinj.rpc.test.TestServers
import org.consensusj.jsonrpc.groovy.DynamicRpcClient
import org.consensusj.jsonrpc.JsonRpcStatusException
import spock.lang.Ignore
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

    @Ignore("Wait until we're able to require bitcoind 0.11.x or later")
    def "setgenerate"() {
        when:
        def result = client.generate(2)

        then:
        result != null /* Bitcoin 0.10.x or later */
    }

    def "getinfo" () {
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
        e.httpMessage == "Not Found"
        e.httpCode == 404
        e.response == null
        e.responseJson.result instanceof NullNode
        e.responseJson.error.code == -32601
        e.responseJson.error.message == "Method not found"
    }

}