package org.consensusj.jsonrpc

import spock.lang.Ignore
import spock.lang.Specification

import org.consensusj.bitcoin.jsonrpc.RpcURI

/**
 * RPCClient test specification
 */
@Ignore("Integration test")
class DefaultRpcClientSpec extends Specification {
    private final int regTestPort = RpcURI.RPCPORT_REGTEST
    private final URI testServerUri = "http://localhost:${regTestPort}".toURI();
    private final String user = "bitcoinrpc"
    private final String pass = "pass"
    // TODO: Refactor so tests automatically run against both factories.
    private final DefaultRpcClient.TransportFactory transportFactory = (m) -> new JsonRpcClientJavaNet(m, JsonRpcTransport.getDefaultSSLContext(), testServerUri, user, pass)
    //private final DefaultRpcClient.TransportFactory transportFactory = (m) -> new JsonRpcClientHttpUrlConnection(m, JsonRpcTransport.getDefaultSSLContext(), testServerUri, user, pass)

    def "constructor works correctly" () {
        when:
        def client = new DefaultRpcClient(transportFactory, JsonRpcMessage.Version.V2)

        then:
        client.serverURI == "http://localhost:${regTestPort}".toURI()
        client.getJsonRpcVersion() == JsonRpcMessage.Version.V2
    }

// TODO: Create  JsonRpcClientJavaNetSpec that tests the sendRequestForResponseString method

//    def "get block count as string" () {
//        when:
//        def client = new DefaultRpcClient(testServerUri, user, pass)
//        JsonRpcRequest req = new JsonRpcRequest("getblockcount");
//        String blockcount = client.sendRequestForResponseString(req).join()
//
//        then:
//        blockcount instanceof  String
//        blockcount.length() >= 1
//    }

    def "get block count as JsonRpcResponse" () {
        when:
        def client = new DefaultRpcClient(transportFactory, JsonRpcMessage.Version.V2)
        Integer blockcount = client.send("getblockcount", Integer.class)

        then:
        blockcount >= 0
    }
}
