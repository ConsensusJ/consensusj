package org.consensusj.jsonrpc


import spock.lang.Specification
import spock.lang.Unroll

/**
 * RPCClient test specification
 */
class RpcClientSpec extends Specification {


    def "constructor works correctly" () {
        when:
        def client = new RpcClient("http://localhost:8080".toURI(), "user", "pass")

        then:
        client.serverURI == "http://localhost:8080".toURI()
    }

    @Unroll
    def "Base64 works for #input"(String input, String expectedResult) {
        expect:
        expectedResult == RpcClient.base64Encode(input)

        where:
        input               | expectedResult
        "a:b"               | "YTpi"
        "0" * 80            | "MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA="
    }
}
