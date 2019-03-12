package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcErrorException;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcServer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.consensusj.jsonrpc.JsonRpcError.Error.METHOD_NOT_FOUND;
import static org.consensusj.jsonrpc.JsonRpcError.Error.SERVER_EXCEPTION;

/**
 * Interface to wrap a Java class with JsonRpc support.
 */
public interface JsonRpcServerWrapper extends JsonRpcServer {
    /**
     * Get the service object
     *
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
     * Handle a request by calling method, getting a result, and embedding it in response.
     * 
     * @param req The Request POJO
     * @return A future JSON RPC Response
     */
    default CompletableFuture<JsonRpcResponse<?>> call(JsonRpcRequest req) {
        return callMethod(req.getMethod(), req.getParams())
                .handle((r, t) -> resultCompletionHandler(req, r, t));
    }

    /**
     * Map a request plus a result or error into a response
     *
     * @param req The request being services
     * @param result A result object or null
     * @param ex exception or null
     * @return A success or error response as appropriate
     */
    default JsonRpcResponse<?> resultCompletionHandler(JsonRpcRequest req, Object result, Throwable ex) {
        return (result != null) ?
                wrapResult(req, result) :
                wrapError(req, exceptionToError(ex));
    }

    /**
     * Map an exception (typically from callMethod) to an Error POJO
     *
     * @param ex Exception returned from "unwrapped" method
     * @return An error POJO for insertion in a JsonRpcResponse
     */
    default JsonRpcError exceptionToError(Throwable ex) {
        return (ex instanceof JsonRpcErrorException) ?
                ((JsonRpcErrorException) ex).getError() :
                JsonRpcError.of(SERVER_EXCEPTION);
    }

    /**
     * Call an "unwrapped" method from an existing service object
     * 
     * @param methodName Method to call
     * @param params List of JSON-RPC parameters
     * @return A future result POJO
     */
    default CompletableFuture<Object> callMethod(String methodName, List<Object> params) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        MethodHandle mh = getMethodHandle(methodName);
        if (mh != null) {
            try {
                Object[] spParms = getSigPolyParms(params);
                // TODO: Carefully check that `invokeWithArguments` is what we really need
                Object result = mh.invokeWithArguments(spParms);
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(JsonRpcErrorException.of(SERVER_EXCEPTION));
            }
        } else {
            future.completeExceptionally(JsonRpcErrorException.of(METHOD_NOT_FOUND));
        }
        return future;
    }

    /**
     * Build signature-polymorphic parameter list, by prepending
     * serviceObject and converting to an array
     * @param params The RPC parameters
     * @return signature-polymorphic parameter list for MethodHandle#invoke
     */
    default Object[] getSigPolyParms(List<Object> params) {
        // TODO: Find a simpler/optimal way to prepend service object on params
        ArrayList<Object> list = new ArrayList<>(Arrays.asList(getServiceObject()));
        if (params != null && params.size() > 0) {
            list.addAll(params);
        }
        return list.toArray();
    }

    /**
     * Use reflection/introspection to generate a map of method handles.
     * Generally this is called from the constructor of implementing classes.
     * @param apiClass The service class to reflect/introspect
     * @return a map of method names to method handles
     */
    static Map<String, MethodHandle> reflect(Class<?>  apiClass) {
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

    static JsonRpcResponse<?> wrapError(JsonRpcRequest req, JsonRpcError error) {
        return new JsonRpcResponse<>(null, error, req.getJsonrpc(), req.getId());
    }

}
