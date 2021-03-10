package org.consensusj.jsonrpc.daemon;

import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Simple Echo JSON-RPC Service
 * TODO: For some reason we must use javax.inject.Singleton for Micronaut to load this service
 */
@Singleton
public class EchoJsonRpcService extends AbstractJsonRpcService {
    private static Logger log = LoggerFactory.getLogger(EchoJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());

    public EchoJsonRpcService() {
        super(methods);
    }

    public String echo(String message) {
        log.debug("EchoJsonRpcService: echo {}", message);
        return message;
    }

}
