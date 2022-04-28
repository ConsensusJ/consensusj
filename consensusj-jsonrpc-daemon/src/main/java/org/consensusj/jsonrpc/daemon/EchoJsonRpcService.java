package org.consensusj.jsonrpc.daemon;

import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Simple Echo JSON-RPC Service
 */
@Singleton
public class EchoJsonRpcService extends AbstractJsonRpcService {
    private static final Logger log = LoggerFactory.getLogger(EchoJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final String helpString = """
            echo message
            help
    """;

    public EchoJsonRpcService() {
        super(methods);
    }

    public CompletableFuture<String> echo(String message) {
        log.debug("EchoJsonRpcService: echo {}", message);
        return result(message);
    }

    public CompletableFuture<String> help() {
        log.debug("EchoJsonRpcService: help");
        return result(helpString);
    }
}
