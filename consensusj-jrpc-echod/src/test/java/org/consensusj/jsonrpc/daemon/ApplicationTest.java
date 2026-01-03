package org.consensusj.jsonrpc.daemon;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
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
        URI endpoint =  URI.create(server.getURI().toString()+"/");
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("echo", testString);
            assertEquals(testString, result);
        }
    }
    @Test
    void helpMethod() throws IOException {
        var expectedResult  = """
            echo message
            help
            stop
    """;
        URI endpoint =  URI.create(server.getURI().toString()+"/");
        try (var client = new DefaultRpcClient(endpoint, "", "")) {
            String result = (String) client.send("help");
            assertEquals(expectedResult, result);
        }
    }

    @Test
    void helpMethodFail() throws IOException {
        var expectedError = "Server exception: wrong number of arguments: 1 expected: 0";
        URI endpoint =  URI.create(server.getURI().toString()+"/");
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    try (var client = new DefaultRpcClient(endpoint, "", "")) {
                        client.send("help", "echo");
                    }
                });
        assertEquals(expectedError, exception.getMessage());
    }

}
