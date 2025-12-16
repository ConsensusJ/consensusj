package org.consensusj.jrpc

import org.consensusj.jsonrpc.cli.test.CLITestSupport
import spock.lang.Ignore
import spock.lang.Specification

import java.util.spi.ToolProvider

class JRpcToolSpec extends Specification {
    static final expectedURI = URI.create('http://bitcoinrpc:pass@localhost:18443/')
    static final String TOOL_NAME = "jrpc"
    static final String[] dummyArgs = ['-url', expectedURI, 'getblockcount'].toArray()
    static final String[] helpArgs = ['--help'].toArray()
    static final String[] emptyArgs = new String[0];

    def "Can instantiate via ToolProvider"() {
        when:
        def tool = ToolProvider.findFirst(TOOL_NAME).get()

        then:
        tool instanceof JRpc
    }

    def "Can Run -help via ToolProvider"() {
        when:
        var tool = ToolProvider.findFirst(TOOL_NAME).get()

        then:
        (tool instanceof JRpc)

        when:
        var result = CLITestSupport.runTool(tool as JRpc, helpArgs)

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
        var tool = ToolProvider.findFirst(TOOL_NAME).get()

        then:
        (tool instanceof JRpc)

        when:
        var result = CLITestSupport.runTool(tool as JRpc, emptyArgs)

        then:
        result.status() == 1
        result.output().isEmpty()
        result.error().contains("usage:")
    }

    def "Can create a Call object properly"() {
        given:
        JRpc tool = new JRpc()

        when:
        def call = tool.createCall(System.out, System.err, dummyArgs)
        def client = call.rpcClient();

        then:
        call.out instanceof PrintWriter
        call.err instanceof PrintWriter
        call.args == dummyArgs
        client.getServerURI() == expectedURI
        call instanceof JRpc.CommonsCLICall
    }

    @Ignore("Functional test")
    def "Can call a local Bitcoin server correctly"() {
        given:
        JRpc tool = new JRpc()

        when:
        int result = tool.run(System.out, System.err, dummyArgs)

        then:
        result == 0
    }

    @Ignore("Functional test")
    def "Can call a local Bitcoin and get help correctly"() {
        given:
        JRpc tool = new JRpc()
        String[] helpArgs = ['-url', expectedURI, 'help']

        when:
        int result = tool.run(System.out, System.err, helpArgs)

        then:
        result == 0
    }
}
