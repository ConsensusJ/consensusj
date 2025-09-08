package org.consensusj.jsonrpc.cli

import org.consensusj.jsonrpc.cli.test.CLITestSupport
import spock.lang.Ignore
import spock.lang.Specification

import java.util.spi.ToolProvider

class GenericJsonRpcToolSpec extends Specification {
    static final expectedURI = URI.create('http://bitcoinrpc:pass@localhost:8332/')
    static final String[] dummyArgs = ['-url', expectedURI, 'getblockcount'].toArray()
    static final String[] helpArgs = ['--help'].toArray()
    static final String[] emptyArgs = new String[0];

    def "Can instantiate via ToolProvider"() {
        when:
        def tool = ToolProvider.findFirst("jsonrpc").get()

        then:
        tool instanceof GenericJsonRpcTool
    }

    def "Can Run -help via ToolProvider"() {
        when:
        var tool = ToolProvider.findFirst("jsonrpc").get()

        then:
        tool instanceof GenericJsonRpcTool

        when:
        var result = CLITestSupport.runTool(tool as BaseJsonRpcTool, helpArgs)

        then:
        result.status() == 0
        result.output() != null
        result.error().isEmpty()

        when:
        var output = result.output()

        then:
        output.contains("usage:")
    }

    def "Usage error (no args) via ToolProvider"() {
        when:
        var tool = ToolProvider.findFirst("jsonrpc").get()

        then:
        tool instanceof GenericJsonRpcTool

        when:
        var result = CLITestSupport.runTool(tool as BaseJsonRpcTool, emptyArgs)

        then:
        result.status() == 1
        result.output().isEmpty()
        result.error().contains("usage:")
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

    @Ignore("Functional test")
    def "Can call a local Bitcoin and get help correctly"() {
        given:
        GenericJsonRpcTool tool = new GenericJsonRpcTool()
        String[] helpArgs = ['-url', expectedURI, 'help']

        when:
        int result = tool.run(System.out, System.err, helpArgs)

        then:
        result == 0
    }
}
