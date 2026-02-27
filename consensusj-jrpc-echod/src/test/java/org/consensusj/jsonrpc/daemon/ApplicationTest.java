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
package org.consensusj.jsonrpc.daemon;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic Integration test of the JsonRpc echo daemon
 */
@MicronautTest
public class ApplicationTest {
    @Inject
    EmbeddedServer server;

    URI endpoint;

    @BeforeEach
    void testSetup() {
        endpoint = URI.create(server.getURI().toString());
    }

    @Test
    void serverStarts() {
        assertTrue(server.isRunning());
        assertEquals("http", server.getURI().getScheme());
        assertTrue(
            server.getURI().getHost().equals("localhost") ||
            server.getURI().getHost().startsWith("runner") // for GitLab
        );
    }

    @Test
    void echoMethod() throws IOException {
        var testString  = "Hello jrpc-echod!";
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("echo", testString);
            assertEquals(testString, result);
        }
    }

    @Test
    void echoMethodWrongNumberOfArgs() throws IOException {
        int expectedErrorCode = JsonRpcError.Error.INVALID_PARAMS.getCode();
        var expectedErrorMessagePrefix = "Invalid params:";
        var testString  = "Hello jrpc-echod!";
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("echo", testString, testString);
                    }
                });
        assertTrue(exception.getMessage().startsWith(expectedErrorMessagePrefix));
        assertEquals(expectedErrorCode, exception.jsonRpcCode);
    }

    @Test
    void helpForHelpMethod() throws IOException {
        var expectedResultSubstring  = "Displays detailed help text for the specified method.";
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help", "help");
            assertTrue(result.contains(expectedResultSubstring));
        }
    }

    @Test
    void helpForEchoMethod() throws IOException {
        var expectedResultSubstring  = "Returns the provided message exactly as it was sent.";
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help", "echo");
            assertTrue(result.contains(expectedResultSubstring));
        }
    }

    @Test
    void helpForStopMethod() throws IOException {
        var expectedResultSubstring  = "Initiates the shutdown process of the JSON-RPC server.";
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help", "stop");
            assertTrue(result.contains(expectedResultSubstring));
        }
    }

    @Test
    void helpMethodNoArg() throws IOException {
        var expectedResult  = "echo message\n" +
                "help (method)\n" +
                "stop ";
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help");
            assertEquals(expectedResult, result);
        }
    }

    @Test
    void invalidMethod() throws IOException {
        int expectedErrorCode = JsonRpcError.Error.METHOD_NOT_FOUND.getCode();
        var expectedErrorMessagePrefix = "Method not found:";
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("invalid");
                    }
                });
        assertTrue(exception.getMessage().startsWith(expectedErrorMessagePrefix));
        assertEquals(expectedErrorCode, exception.jsonRpcCode);
    }

}
