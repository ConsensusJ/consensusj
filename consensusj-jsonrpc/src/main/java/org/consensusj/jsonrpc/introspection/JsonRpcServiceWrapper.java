package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcErrorException;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.consensusj.jsonrpc.JsonRpcError.Error.METHOD_NOT_FOUND;
import static org.consensusj.jsonrpc.JsonRpcError.Error.SERVER_EXCEPTION;

/**
 * Interface to wrap a Java class with JsonRpc support.
 */
public interface JsonRpcServiceWrapper extends JsonRpcService {
    static final Logger log = LoggerFactory.getLogger(JsonRpcServiceWrapper.class);

    /**
     * Get the service object
     *
     * @return the service object
     */
    Object getServiceObject();

    /**
     * Get a Method object for a named method
     * @param methodName the name of the method to call
     * @return method handle
     */
    Method getMethod(String methodName);

    /**
     * Handle a request by calling method, getting a result, and embedding it in response.
     * 
     * @param req The Request POJO
     * @return A future JSON RPC Response
     */
    default <R> CompletableFuture<JsonRpcResponse<R>> call(final JsonRpcRequest req) {
        log.debug("JsonRpcServiceWrapper.call: {}", req.getMethod());
        CompletableFuture<R> result = callMethod(req.getMethod(), req.getParams());
        return result.handle((R r, Throwable ex) -> resultCompletionHandler(req, r, ex));
    }
    
    /**
     * Map a request plus a result or error into a response
     *
     * @param req The request being services
     * @param result A result object or null
     * @param ex exception or null
     * @return A success or error response as appropriate
     */
    default <T> JsonRpcResponse<T> resultCompletionHandler(JsonRpcRequest req, T result, Throwable ex) {
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
    default <R> CompletableFuture<R> callMethod(String methodName, List<Object> params) {
        log.debug("JsonRpcServiceWrapper.callMethod: {}", methodName);
        CompletableFuture<R> future = new CompletableFuture<>();
        final Method mh = getMethod(methodName);
        if (mh != null) {
            try {
                R result = (R) mh.invoke(getServiceObject(), params.toArray());
                future.complete(result);
            } catch (Throwable throwable) {
                log.error("Exception in invoked service object: ", throwable);
                JsonRpcErrorException jsonRpcException = new JsonRpcErrorException(SERVER_EXCEPTION, throwable);
                future.completeExceptionally(jsonRpcException);
            }
        } else {
            future.completeExceptionally(JsonRpcErrorException.of(METHOD_NOT_FOUND));
        }
        return future;
    }
    
    /**
     * Use reflection/introspection to generate a map of methods.
     * Generally this is called from the constructor of implementing classes.
     * @param apiClass The service class to reflect/introspect
     * @return a map of method names to method objects
     */
    static Map<String, Method> reflect(Class<?>  apiClass) {
        java.lang.reflect.Method[] publicInheritedMethods = apiClass.getMethods();
        Map<String, Method> methods = new HashMap<>();
        for (Method method : publicInheritedMethods) {
            String name = method.getName();
            methods.put(name, method);
        }
        return methods;
    }

    /**
     * Wrap a Result POJO in a JsonRpcResponse
     * @param req The request we are responding to
     * @param result the result to wrap
     * @return A valid JsonRpcResponse
     */
    static <T> JsonRpcResponse<T> wrapResult(JsonRpcRequest req, T result) {
        return new JsonRpcResponse<>(result, null, req.getJsonrpc(), req.getId());
    }

    static <T> JsonRpcResponse<T> wrapError(JsonRpcRequest req, JsonRpcError error) {
        return new JsonRpcResponse<>(null, error, req.getJsonrpc(), req.getId());
    }
}
