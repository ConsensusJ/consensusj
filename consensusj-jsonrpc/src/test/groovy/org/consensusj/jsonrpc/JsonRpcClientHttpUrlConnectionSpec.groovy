package org.consensusj.jsonrpc


import spock.lang.Specification
import spock.lang.Unroll

/**
 * RPCClient test specification
 */
class JsonRpcClientHttpUrlConnectionSpec extends Specification {


    def "constructor works correctly" () {
        when:
        def client = new JsonRpcClientHttpUrlConnection(JsonRpcMessage.Version.V2, "http://localhost:8080".toURI(), "user", "pass")

        then:
        client.serverURI == "http://localhost:8080".toURI()
        client.getJsonRpcVersion() == JsonRpcMessage.Version.V2
    }

    @Unroll
    def "Base64 works for #input"(String input, String expectedResult) {
        expect:
        expectedResult == JsonRpcClientHttpUrlConnection.base64Encode(input)

        where:
        input               | expectedResult
        "a:b"               | "YTpi"
        "0" * 80            | "MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA="
    }
}
