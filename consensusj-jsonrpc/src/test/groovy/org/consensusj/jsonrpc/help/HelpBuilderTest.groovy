package org.consensusj.jsonrpc.help


import org.consensusj.jsonrpc.introspection.sample.MathService
import spock.lang.Specification

/**
 * Quick test of JsonRpcServiceWrapper
 */
class HelpBuilderTest extends Specification {
    def "reflect works"() {
        given:
        var serviceClass = new MathService().class

        when:
        var result = JsonRpcHelp.mapOf(serviceClass)

        then:
        result.size() == 3
        result.get("add") instanceof JsonRpcHelp;
        result.get("subtract") instanceof JsonRpcHelp;

        when:
        var addHelp = result.get("add")
        var subtractHelp = result.get("subtract")

        then:
        addHelp.summary() == "adds two numbers"
        addHelp.detail() == "This method takes two numeric parameters and returns their sum"
        subtractHelp.summary() == "subtracts two numbers"
        subtractHelp.detail() == "This method takes two numeric parameters and returns their difference"
    }
}
