package org.consensusj.jsonrpc.introspection

import org.consensusj.jsonrpc.JsonRpcRequest
import org.consensusj.jsonrpc.JsonRpcResponse
import spock.lang.Specification

/**
 * Quick smoke test of DelegatingJsonRpcService
 */
class DelegatingJsonRpcServiceTest extends Specification {
    def "callMethod works"() {
        given:
        var unwrapped = new TrivialJsonRpcService()
        var wrapped = new DelegatingJsonRpcService(unwrapped)

        when:
        JsonRpcResponse<Integer> response = wrapped.call(new JsonRpcRequest("getblockcount")).get()

        then:
        response.result == 99
    }
}
