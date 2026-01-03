package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.ObjectMapper
import org.jspecify.annotations.Nullable
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test Spec for JsonRpcRequest
 */
class JsonRpcRequestSpec extends Specification {
    @Unroll
    def "removeTrailingNulls does the right thing for input: #input"(List<@Nullable Object> input, List<@Nullable Object> expectedOutput) {
        when:
        List<@Nullable Object> out = JsonRpcRequest.removeTrailingNulls(input)

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

    @Unroll
    def "Can construct JsonRpcRequest with params: #inputParams"(List<@Nullable Object> inputParams, List<@Nullable Object> expectedParams) {
        when:
        JsonRpcRequest req = new JsonRpcRequest("dummy", inputParams)

        then: "the request was constructed with trailing nulls removed"
        expectedParams == req.getParams()

        where:
        inputParams                 | expectedParams
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
        var mapper = new ObjectMapper()
        var json = """
{"jsonrpc":"1.0", "id":1, "method":"getblockcount", "params":[]}
"""

        when:
        JsonRpcRequest request = mapper.readValue(json, JsonRpcRequest.class)

        then:
        request != null
    }

    def "can create from JSON -- without version"() {
        given:
        var mapper = new ObjectMapper()
        var json = """
{"id":1, "method":"getblockcount", "params":[]}
"""

        when:
        JsonRpcRequest request = mapper.readValue(json, JsonRpcRequest.class)

        then:
        request != null
    }
}