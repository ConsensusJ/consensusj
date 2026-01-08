package org.consensusj.jsonrpc.daemon;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.consensusj.jsonrpc.DefaultRpcClient;
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
        var expectedError = "Server exception: wrong number of arguments: 2 expected: 1";
        var testString  = "Hello jrpc-echod!";
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("echo", testString, testString);
                    }
                });
        assertEquals(expectedError, exception.getMessage());
    }


    @Test
    void helpMethod() throws IOException {
        var expectedResult  = """
            echo message
            help
            stop
    """;
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help");
            assertEquals(expectedResult, result);
        }
    }

    /*
     * The help method is currently not fully implemented. It SHOULD allow
     * for an argument, and only fail if the argument doesn't match an existing
     * command. Once the help method is properly implemented we will need to change
     * our tests
     */
    @Test
    void helpMethodOneArg() throws IOException {
        var expectedError = "Server exception: wrong number of arguments: 1 expected: 0";
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("help", "echo");
                    }
                });
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void invalidMethod() throws IOException {
        var expectedError = "Method not found";
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("invalid");
                    }
                });
        assertEquals(expectedError, exception.getMessage());
    }

}
