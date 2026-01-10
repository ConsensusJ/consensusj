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

    def "can create from JSON -- with version 1.0"() {
        given:
        var json = """
            {"jsonrpc":"1.0", "id":1, "method":"getblockcount", "params":[]}
        """

        when:
        JsonRpcRequest request = parse(json)

        then:
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- with version 2.0"() {
        given:
        var json = """
            {"jsonrpc":"2.0", "id":1, "method":"getblockcount", "params":[]}
        """
        when:
        JsonRpcRequest request = parse(json)

        then:
        request.id == "1"
        request.jsonrpc == "2.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- without version"() {
        given:
        var json = """
            {"id":1, "method":"getblockcount", "params":[]}
        """

        when:
        JsonRpcRequest request = parse(json)

        then: "A request is created (I believe this is legal in JSON-RPC 1.0)"
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- without params"() {
        given:
        var json = """
            {"id":1, "method":"getblockcount"}
        """

        when:
        JsonRpcRequest request = parse(json)

        then:
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    static final ObjectMapper mapper = new ObjectMapper()
    private JsonRpcRequest parse(String json) {
        return mapper.readValue(json, JsonRpcRequest.class)
    }
}
