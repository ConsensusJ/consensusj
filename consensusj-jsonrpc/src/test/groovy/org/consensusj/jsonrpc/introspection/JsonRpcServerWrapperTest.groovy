package org.consensusj.jsonrpc.introspection

import spock.lang.Specification

import java.lang.invoke.MethodHandle

/**
 *
 */
class JsonRpcServerWrapperTest extends Specification {
    def "reflect works"() {
        given:
        def unwrapped = new Echo()

        when:
        def result = JsonRpcServerWrapper.reflect(unwrapped)

        then:
        result.size() >= 1      // TODO: This is 17 because of inherited methods form Object
        result.get("echo") instanceof MethodHandle
    }

    class Echo {
        Integer echo(Integer input) {
            return input;
        }
    }
}