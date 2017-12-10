package org.consensusj.jsonrpc

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

/**
 * RPCClient test specification
 */
class RPCClientSpec extends Specification {


    def "constructor works correctly" () {
        when:
        def client = new RPCClient("http://localhost:8080".toURI(), "user", "pass")

        then:
        client.serverURI == "http://localhost:8080".toURI()
    }

    @Ignore
    @Unroll
    def "Base64 works for #input"(String input, String expectedResult) {
        expect:
        expectedResult == RPCClient.base64Encode(input)

        where:
        input               | expectedResult
        "a:b"               | "YTpi"
        //"0" * 80            | "MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA="
    }
}
