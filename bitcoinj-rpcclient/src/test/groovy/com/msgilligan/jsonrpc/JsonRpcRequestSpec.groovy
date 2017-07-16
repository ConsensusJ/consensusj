package com.msgilligan.jsonrpc

import com.msgilligan.jsonrpc.JsonRpcRequest
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
}