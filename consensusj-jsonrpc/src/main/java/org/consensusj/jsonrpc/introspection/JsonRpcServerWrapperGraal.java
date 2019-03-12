package org.consensusj.jsonrpc.introspection;

import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * Graal-compatible implementation that takes a map of MethodHandles in the constructor
 * so that introspection can be done at static initialization time
 * (which means during native image generation)
 */
public abstract class JsonRpcServerWrapperGraal implements JsonRpcServerWrapper {
    private final Map<String, MethodHandle> methods;

    public JsonRpcServerWrapperGraal(Map<String, MethodHandle> methods) {
        this.methods = methods;
    }

    @Override
    public Object getServiceObject() {
        return this;
    }

    @Override
    public MethodHandle getMethodHandle(String methodName) {
        return methods.get(methodName);
    }

}
