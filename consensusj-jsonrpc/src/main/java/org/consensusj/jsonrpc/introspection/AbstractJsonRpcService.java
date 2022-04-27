package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.AsyncSupport;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Graal-compatible implementation that takes a map of Methods in the constructor
 * so that introspection can be done at static initialization time. Assumes
 * implementation is in a subclass.
 * (which means during native image generation)
 */
public abstract class AbstractJsonRpcService implements JsonRpcServiceWrapper {
    protected Map<String, Method> methods;

    public AbstractJsonRpcService(Map<String, Method> methods) {
        this.methods = methods;
    }

    @Override
    public Object getServiceObject() {
        return this;
    }

    @Override
    public Method getMethod(String methodName) {
        return methods.get(methodName);
    }

    protected <T> CompletableFuture<T> result(T result) {
        return CompletableFuture.completedFuture(result);
    }

    protected <T> CompletableFuture<T> exception(Throwable exception) {
        return AsyncSupport.failedFuture(exception);
    }
}
