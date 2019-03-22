package org.consensusj.jsonrpc.introspection


import spock.lang.Specification

/**
 * Quick smoke test of DelegatingJsonRpcService
 */
class DelegatingJsonRpcServiceTest extends Specification {
    def "callMethod works"() {
        given:
        def unwrapped = new TrivialJsonRpcService()
        def wrapped = new DelegatingJsonRpcService(unwrapped)

        when:
        def result = wrapped.callMethod("getblockcount", []).get()

        then:
        result == 99
    }
}
