package org.consensusj.jsonrpc.cli

import spock.lang.Ignore
import spock.lang.Specification

import java.util.spi.ToolProvider

/**
 *
 */
class GenericJsonRpcToolSpec extends Specification {
    static final expectedURI = URI.create('http://bitcoinrpc:pass@localhost:8332/')
    static final String[] dummyArgs = ['-url', expectedURI, 'getblockcount'].toArray()

    def "Can instantiate via ToolProvider"() {
        when:
        def tool = ToolProvider.findFirst("jsonrpc").get()

        then:
        tool instanceof GenericJsonRpcTool
    }

    def "Can create a Call object properly"() {
        given:
        GenericJsonRpcTool tool = new GenericJsonRpcTool()

        when:
        def call = tool.createCall(System.out, System.err, dummyArgs)
        def client = call.rpcClient();

        then:
        call.out instanceof PrintWriter
        call.err instanceof PrintWriter
        call.args == dummyArgs
        client.getServerURI() == expectedURI
        call instanceof BaseJsonRpcTool.CommonsCLICall
    }

    @Ignore("Functional test")
    def "Can call a local Bitcoin server correctly"() {
        given:
        GenericJsonRpcTool tool = new GenericJsonRpcTool()
        
        when:
        int result = tool.run(System.out, System.err, dummyArgs)

        then:
        result == 0
    }
}
