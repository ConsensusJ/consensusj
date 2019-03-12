package org.consensusj.jsonrpc.introspection;

import java.lang.invoke.MethodHandle;
import java.util.Map;

/**
 * Implementation of JsonRpcServerWrapper that takes a delegate
 * object in the constructor
 */
public class JsonRpcServerWrapperImpl implements JsonRpcServerWrapper {
    private final Object service;
    private final Map<String, MethodHandle> methods;

    public JsonRpcServerWrapperImpl(Object serviceObject) {
        this(serviceObject, JsonRpcServerWrapper.reflect(serviceObject.getClass()));
    }

    public JsonRpcServerWrapperImpl(Object serviceObject, Map<String, MethodHandle> methods) {
        this.service = serviceObject;
        this.methods = methods;
    }

    @Override
    public Object getServiceObject() {
        return service;
    }

    @Override
    public MethodHandle getMethodHandle(String methodName) {
        return methods.get(methodName);
    }
}
