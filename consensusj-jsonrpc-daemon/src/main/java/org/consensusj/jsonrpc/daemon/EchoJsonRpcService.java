package org.consensusj.jsonrpc.daemon;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Simple Echo JSON-RPC Service
 */
@Singleton
public class EchoJsonRpcService extends AbstractJsonRpcService implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(EchoJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final String helpString = """
            echo message
            help
            stop
    """;

    private EmbeddedServer embeddedServer;

    public EchoJsonRpcService() {
        super(methods);
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Saving reference to embeddedServer");
        embeddedServer = event.getSource();
    }

    @EventListener
    public void onShutdown(ServerShutdownEvent event) {
        log.info("Shutting down");
        this.close();
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
        embeddedServer.stop();
        var appName = embeddedServer.getApplicationConfiguration().getName().orElse("server");
        return result(appName + " stopping");
    }
}
