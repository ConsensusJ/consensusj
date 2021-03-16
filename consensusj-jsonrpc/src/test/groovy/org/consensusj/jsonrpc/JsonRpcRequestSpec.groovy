package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Unroll


/**
 * Test Spec for JsonRpcRequest
 */
class JsonRpcRequestSpec extends Specification {
    @Unroll
    def "removeTrailingNulls does the right thing for input: #input"() {
        when:
        def out = JsonRpcRequest.removeTrailingNulls(input)

        then:
        out == expectedOutput

        where:
        input                       | expectedOutput
        []                          | []
        [1]                         | [1]
        [null]                      | []
        [1, null]                   | [1]
        [null, 1]                   | [null, 1]
        [null, 1, null]             | [null, 1]
        [1, null, 2]                | [1, null, 2]
        [1, null, 2, null]          | [1, null, 2]
        [1, null, 2, null, 3, null] | [1, null, 2, null, 3]
    }

    def "can create from JSON -- with version"() {
        given:
        def mapper = new ObjectMapper()
        def json = """
{"jsonrpc":"1.0", "id":1, "method":"getblockcount", "params":[]}
"""

        when:
        JsonRpcRequest request = mapper.readValue(json, JsonRpcRequest.class)

        then:
        request != null
    }

    def "can create from JSON -- without version"() {
        given:
        def mapper = new ObjectMapper()
        def json = """
{"id":1, "method":"getblockcount", "params":[]}
"""

        when:
        JsonRpcRequest request = mapper.readValue(json, JsonRpcRequest.class)

        then:
        request != null
    }

}