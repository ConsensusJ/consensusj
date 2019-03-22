package org.consensusj.jsonrpc.introspection;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Implementation of JsonRpcServerWrapper that takes a delegate
 * object in the constructor
 */
public class DelegatingJsonRpcService extends AbstractJsonRpcService {
    private final Object service;

    /**
     * Use this constructor for simplicity and if you don't need GraalVM support
     * @param serviceObject
     */
    public DelegatingJsonRpcService(Object serviceObject) {
        this(JsonRpcServiceWrapper.reflect(serviceObject.getClass()), serviceObject);
    }

    /**
     * Use this constructor for GraalVM compatibility (methods generated in static initializer)
     *
     * @param serviceObject
     * @param methods
     */
    public DelegatingJsonRpcService(Map<String, Method> methods, Object serviceObject) {
        super(methods);
        this.service = serviceObject;
    }

    @Override
    public Object getServiceObject() {
        return service;
    }

    @Override
    public Method getMethod(String methodName) {
        return methods.get(methodName);
    }
}
