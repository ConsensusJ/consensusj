package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.AsyncSupport;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GraalVM-compatible implementation of {@link JsonRpcServiceWrapper} that takes a map of {@link Method}s in the constructor
 * so that introspection can be done at static initialization time (which means during native image generation).
 * Since {@link #getServiceObject()} returns {@code this} you typically directly subclass {@code AbstractJsonRpcService}.
 * For example see {@link org.consensusj.jsonrpc.introspection.sample.MathService}.
 */
public abstract class AbstractJsonRpcService implements JsonRpcServiceWrapper {
    protected final Map<String, Method> methods;

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

    protected <RSLT> CompletableFuture<RSLT> result(RSLT result) {
        return CompletableFuture.completedFuture(result);
    }

    protected <RSLT> CompletableFuture<RSLT> exception(Throwable exception) {
        return CompletableFuture.failedFuture(exception);
    }
}
