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
package org.consensusj.jsonrpc.services;

import org.consensusj.jsonrpc.JsonRpcShutdownService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test a {@link EchoJsonRpcService} instance.
 */
public class EchoJsonRpcServiceTest {
    final EchoJsonRpcService service = new EchoJsonRpcService(new JsonRpcShutdownService.NoopShutdownService());;

    @AfterEach
    void cleanUp() {
        service.close();
    }

    @Test
    void testEcho() {
        String hello = service.echo("hello").join();
        assertEquals("hello", hello);

    }

    @Test
    void testHelpSummary() {
        String help = service.help(/* null */).join();
        assertEquals("echo message\nhelp\nstop\n", help);
    }

    @Test
    @Disabled
    void testHelpDetail() {
        String help = service.help(/* "help" */).join();
        assertEquals("help help", help);
    }
}
