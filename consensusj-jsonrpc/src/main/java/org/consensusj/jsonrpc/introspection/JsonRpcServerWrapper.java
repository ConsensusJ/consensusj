package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcServer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface to wrap a Java class with JsonRpc support.
 */
public interface JsonRpcServerWrapper extends JsonRpcServer {
    /**
     *
     * @param req The Request POJO
     * @return
     */
    default CompletableFuture<JsonRpcResponse<?>> call(JsonRpcRequest req) {
        return callMethod(req.getMethod(), req.getParams())
                .thenApply(r -> wrapResult(req, r));
    }

    /**
     * Call an "unwrapped" method from an existing service object
     * 
     * @param methodName Method to call
     * @param params List of JSON-RPC parameters
     * @return result POJO
     */
    default CompletableFuture<Object> callMethod(String methodName, List params) {
        Object result;
        MethodHandle mh = getMethodHandle(methodName);
        if (mh != null) {
            try {
                //Object[] params = req.getParams().toArray();
                result = mh.invoke(getServiceObject() /*, req.getParams().toArray() */);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                // TODO: Create error response
                result = null;
            }
        } else {
            // TODO: Create error response
            result = null;
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Get the service object
     * @return the service object
     */
    Object getServiceObject();

    /**
     * Get a MethodHandle for a method
     * @param methodName the name of the method to call
     * @return method handle
     */
    MethodHandle getMethodHandle(String methodName);

    /**
     * Use reflection/introspection to generate a map of method handles.
     * Generally this is called from the constructor of implementing classes.
     * @param apiObj The service object to reflect/introspect
     * @return a map of method names to method handles
     */
    static Map<String, MethodHandle> reflect(Object apiObj) {
        Class<?> apiClass = apiObj.getClass();
        java.lang.reflect.Method[] publicInheritedMethods = apiClass.getMethods();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Map<String, MethodHandle> methods = new HashMap<>();
        for (Method method : publicInheritedMethods) {
            String name = method.getName();
            MethodHandle handle;
            try {
                handle = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                continue;   // no access, Skip this one
            }
            methods.put(name, handle);
        }
        return methods;
    }

    /**
     * Wrap a Result POJO in a JsonRpcResponse
     * @param req The request we are responding to
     * @param result the result to wrap
     * @return A valid JsonRpcResponse
     */
    static JsonRpcResponse<?> wrapResult(JsonRpcRequest req, Object result) {
        return new JsonRpcResponse<>(result, null, req.getJsonrpc(), req.getId());
    }

}
