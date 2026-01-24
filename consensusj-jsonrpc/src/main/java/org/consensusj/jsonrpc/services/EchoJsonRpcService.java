package org.consensusj.jsonrpc.services;

import org.consensusj.jsonrpc.JsonRpcShutdownService;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Simple Echo JSON-RPC Service
 */
public class EchoJsonRpcService extends AbstractJsonRpcService implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(EchoJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final String helpString =
            "echo message\n" +
            "help\n" +
            "stop\n";

    private final JsonRpcShutdownService shutdownService;

    public EchoJsonRpcService(JsonRpcShutdownService shutdownService) {
        super(methods);
        this.shutdownService = shutdownService;
    }

    @Override
    public void close() {
        log.info("Closing");
    }

    public CompletableFuture<String> echo(String message) {
        log.debug("EchoJsonRpcService: echo {}", message);
        return result(message);
    }

    public CompletableFuture<String> help() {
        log.debug("EchoJsonRpcService: help");
        return result(helpString);
    }

    /**
     * Initiate server shutdown. This is a JSON-RPC method and will initiate but not
     * complete server-shutdown because it must return a response to the client.
     * @return A status string indicating the server is stopping
     */
    public CompletableFuture<String> stop() {
        log.info("stop");
        String message = shutdownService.stopServer();
        return result(message);
    }
}
