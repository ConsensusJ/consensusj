package org.consensusj.jsonrpc.javanet

import org.consensusj.jsonrpc.JsonRpcRequest
import spock.lang.Ignore
import spock.lang.Specification

import org.consensusj.bitcoin.jsonrpc.RpcURI

/**
 * RPCClient test specification
 */
@Ignore("Integration test")
class JsonRpcClientJavaNetSpec extends Specification {
    private final int regTestPort = RpcURI.RPCPORT_REGTEST
    private final URI testServerUri = "http://localhost:${regTestPort}".toURI();
    private final String user = "bitcoinrpc"
    private final String pass = "pass"


    def "constructor works correctly" () {
        when:
        def client = new JsonRpcClientJavaNet(testServerUri, user, pass)

        then:
        client.serverURI == "http://localhost:${regTestPort}".toURI()
        client.getJsonRpcVersion() == JsonRpcMessage.Version.V2
    }

    def "get block count as string" () {
        when:
        def client = new JsonRpcClientJavaNet(testServerUri, user, pass)
        JsonRpcRequest req = new JsonRpcRequest("getblockcount");
        String blockcount = client.sendRequestForResponseString(req)

        then:
        blockcount instanceof  String
        blockcount.length() >= 1
    }

    def "get block count as JsonRpcResponse" () {
        when:
        def client = new JsonRpcClientJavaNet(testServerUri, user, pass)
        Integer blockcount = client.send("getblockcount", Integer.class)

        then:
        blockcount >= 0
    }
}
