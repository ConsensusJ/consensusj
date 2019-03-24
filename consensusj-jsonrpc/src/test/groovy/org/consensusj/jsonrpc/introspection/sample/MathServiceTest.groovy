package org.consensusj.jsonrpc.introspection.sample

import org.consensusj.jsonrpc.JsonRpcRequest
import org.consensusj.jsonrpc.JsonRpcResponse
import spock.lang.Ignore
import spock.lang.Specification

/**
 *  Quick test of MathService
 */
class MathServiceTest extends Specification {
    def "Main"() {
        when:
        MathService.main();

        then:
        noExceptionThrown()
    }

    def "add"() {
        given:
        def mathService = new MathService();

        when:
        def sum = mathService.add(2, 1)

        then:
        sum == 3
    }

    def "JSON-RPC add"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 3
    }

    def "JSON-RPC subtract"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("subtract", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 1
    }

    @Ignore
    def "JSON-RPC add of strings"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList("2", "1"));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 3
    }

}
