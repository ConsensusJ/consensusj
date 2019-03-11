package org.consensusj.jsonrpc.introspection

import spock.lang.Specification

/**
 *
 */
class JsonRpcServerWrapperImplTest extends Specification {
    def "callMethod works"() {
        given:
        def wrapped = new JsonRpcServerWrapperImpl(new TestServiceObject())

        when:
        def result = wrapped.callMethod("one", []).get()

        then:
        result == 1
    }

    class TestServiceObject {
        Integer one() {
            return 1;
        }
    }

}
