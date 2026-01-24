package org.consensusj.jsonrpc.introspection;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Implementation of {@link JsonRpcServiceWrapper} that takes a delegate
 * object in the constructor.
 * <p>
 * Use this class when
 */
public class DelegatingJsonRpcService extends AbstractJsonRpcService {
    private final Object service;

    /**
     * Use this constructor for simplicity and if you don't need GraalVM support
     * @param serviceObject the service object to wrap.
     */
    public DelegatingJsonRpcService(Object serviceObject) {
        this(JsonRpcServiceWrapper.reflect(serviceObject.getClass()), serviceObject);
    }

    /**
     * Use this constructor for GraalVM compatibility and make sure your {@code methods} {@link Map}
     * was statically initialized.
     *
     * @param serviceObject The service object to wrap
     * @param methods A map that maps method-name strings to {@link Method} objects.
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
    public void close() throws Exception {
    }
}
