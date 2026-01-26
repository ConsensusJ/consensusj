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
        input                         | expectedOutput
        pl()                          | pl()
        pl(1)                         | pl(1)
        pl([null] as Object[])        | pl()
        pl(1, null)                   | pl(1)
        pl(null, 1)                   | pl(null, 1)
        pl(null, 1, null)             | pl(null, 1)
        pl(1, null, 2)                | pl(1, null, 2)
        pl(1, null, 2, null)          | pl(1, null, 2)
        pl(1, null, 2, null, 3, null) | pl(1, null, 2, null, 3)
    }

    @Unroll
    def "Can construct JsonRpcRequest with params: #inputParams"(List<@Nullable Object> inputParams, List<@Nullable Object> expectedParams) {
        when:
        JsonRpcRequest req = new JsonRpcRequest("dummy", inputParams)

        then: "the request was constructed with trailing nulls removed"
        expectedParams == req.getParams()

        where:
        inputParams                   | expectedParams
        pl()                          | pl()
        pl(1)                         | pl(1)
        pl([null] as Object[])        | pl()
        pl(1, null)                   | pl(1)
        pl(null, 1)                   | pl(null, 1)
        pl(null, 1, null)             | pl(null, 1)
        pl(1, null, 2)                | pl(1, null, 2)
        pl(1, null, 2, null)          | pl(1, null, 2)
        pl(1, null, 2, null, 3, null) | pl(1, null, 2, null, 3)
    }

    def "can create from JSON -- with version 1.0"() {
        when:
        JsonRpcRequest request = parse """
            {"jsonrpc":"1.0", "id":1, "method":"getblockcount", "params":[]}
        """

        then:
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- with version 2.0"() {
        when:
        JsonRpcRequest request = parse """
            {"jsonrpc":"2.0", "id":1, "method":"getblockcount", "params":[]}
        """

        then:
        request.id == "1"
        request.jsonrpc == "2.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- without version"() {
        when:
        JsonRpcRequest request = parse """
            {"id":1, "method":"getblockcount", "params":[]}
        """

        then:
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    def "can create from JSON -- without params"() {
        when:
        JsonRpcRequest request = parse """
            {"id":1, "method":"getblockcount"}
        """

        then:
        request.id == "1"
        request.jsonrpc == "1.0"
        request.method == "getblockcount"
        request.params == Collections.emptyList()
    }

    /**
     * Create <b>p</b>arameter <b>l</b>ist of unmodifiable ArrayList from varargs (can contain nulls) as need for JSON-RPC {@code params}
     * @param params
     * @return
     */
    private static pl(Object... params) {
        return Collections.unmodifiableList(new ArrayList(Arrays.asList(params)))
    }

    static final ObjectMapper mapper = new ObjectMapper()
    private static JsonRpcRequest parse(String json) {
        return mapper.readValue(json, JsonRpcRequest.class)
    }
}
