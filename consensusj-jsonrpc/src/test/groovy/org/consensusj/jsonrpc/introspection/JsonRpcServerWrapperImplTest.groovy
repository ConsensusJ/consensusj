package org.consensusj.jsonrpc.introspection

import groovy.transform.CompileStatic
import spock.lang.Specification

/**
 *
 */
class JsonRpcServerWrapperImplTest extends Specification {
    def "callMethod works"() {
        given:
        def unwrapped = new TrivialJsonRpcService()
        def wrapped = new JsonRpcServerWrapperImpl(unwrapped)

        when:
        def result = wrapped.callMethod("getblockcount", []).get()

        then:
        result == 99
    }
}
