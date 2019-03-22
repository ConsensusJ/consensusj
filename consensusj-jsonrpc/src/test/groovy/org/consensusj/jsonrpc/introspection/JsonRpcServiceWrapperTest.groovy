package org.consensusj.jsonrpc.introspection

import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Quick test of JsonRpcServiceWrapper
 */
class JsonRpcServiceWrapperTest extends Specification {
    def "reflect works"() {
        given:
        def unwrapped = new TrivialJsonRpcService()

        when:
        def result = JsonRpcServiceWrapper.reflect(unwrapped.class)

        then:
        // TODO: size is unknown (bigger than 1) because of inherited methods from Object
        result.size() >= 1
        result.get("getblockcount") instanceof Method
    }
}
