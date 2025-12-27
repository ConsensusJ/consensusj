package org.consensusj.jrpc;

import org.consensusj.jsonrpc.cli.test.CLITestSupport;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.spi.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolProviderTests {
    static final URI expectedURI = URI.create("http://bitcoinrpc:pass@localhost:18443/");
    static final String TOOL_NAME = "jrpc";
    static final String[] dummyArgs = {"-url", expectedURI.toString(), "getblockcount"};
    static final String[] helpArgs = {"--help"};
    static final String[] emptyArgs = {};

    @Test
    void canInstantiate() {
        var tool = ToolProvider.findFirst(TOOL_NAME).get();

        assertNotNull(tool);
        assertInstanceOf(JRpc.class, tool);
    }

    @Test
    void canRunHelp() {
        var tool = ToolProvider.findFirst(TOOL_NAME).get();
        var result = CLITestSupport.runTool(tool, helpArgs);

        assertEquals(0, result.status());
        assertNotNull(result.output());
        assertTrue(result.error().isEmpty());

        var output = result.output();

        assertTrue(output.contains("usage:"));
    }

    @Test
    void noArgsGivesUsageError() {
        var tool = ToolProvider.findFirst(TOOL_NAME).get();
        var result = CLITestSupport.runTool(tool, emptyArgs);

        assertEquals(1, result.status());
        assertTrue(result.output().isEmpty());
        assertTrue(result.error().contains("usage:"));
    }

    @Test
    void createCallObject() {
        var tool = new JRpc();
        var call = tool.createCall(System.out, System.err, dummyArgs);
        var client = call.rpcClient();

        assertEquals(dummyArgs, call.args);
        assertEquals(expectedURI, client.getServerURI());

        client.close();
    }
}
