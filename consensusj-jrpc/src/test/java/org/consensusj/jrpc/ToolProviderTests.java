/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
