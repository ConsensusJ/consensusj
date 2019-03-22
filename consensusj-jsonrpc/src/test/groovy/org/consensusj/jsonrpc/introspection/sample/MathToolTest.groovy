package org.consensusj.jsonrpc.introspection.sample

import org.consensusj.jsonrpc.JsonRpcRequest
import org.consensusj.jsonrpc.JsonRpcResponse
import spock.lang.Specification

/**
 *  Quick test of MathTool
 */
class MathToolTest extends Specification {
    def "Main"() {
        when:
        MathTool.main();

        then:
        noExceptionThrown()
    }

    def "add"() {
        given:
        def sumTool = new MathTool();

        when:
        def sum = sumTool.add(2, 1)

        then:
        sum == 3
    }

    def "JSON-RPC add"() {
        given:
        def sumTool = new MathTool();

        when:
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = sumTool.call(req).get();
        def sum = response.getResult();

        then:
        sum == 3
    }

    def "JSON-RPC subtract"() {
        given:
        def sumTool = new MathTool();

        when:
        JsonRpcRequest req = new JsonRpcRequest("subtract", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = sumTool.call(req).get();
        def sum = response.getResult();

        then:
        sum == 1
    }

}
