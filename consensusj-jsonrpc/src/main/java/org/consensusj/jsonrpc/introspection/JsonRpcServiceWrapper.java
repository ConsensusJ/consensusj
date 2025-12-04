package org.consensusj.jsonrpc.introspection;

import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcErrorException;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.consensusj.jsonrpc.JsonRpcError.Error.METHOD_NOT_FOUND;
import static org.consensusj.jsonrpc.JsonRpcError.Error.SERVER_EXCEPTION;

/**
 * Interface and default methods for wrapping a Java class with JSON-RPC support. The wrapper is responsible for
 * extracting the JSON-RPC {@code method} name and {@code params}from the request, calling the appropriate method
 * in the wrapped object and wrapping the {@code result} in a {@link JsonRpcResponse}.
 * <p>
 * The wrapped class must contain one or more asynchronous methods that return {@link CompletableFuture}s for objects that represent
 * JSON-RPC {@code result} values. They are mapped to JSON objects when serialized (via <b>Jackson</b> in the current implementation.)
 * <p>
 * This interface contains a {@code default} implementation of {@link JsonRpcService#call(JsonRpcRequest)} that
 * uses {@link JsonRpcServiceWrapper#callMethod(String, List)} to call the wrapped service object.
 * <p>
 * Implementations must implement:
 * <ul>
 *     <li>{@link #getServiceObject()} to return the wrapped object (singleton)</li>
 *     <li>{@link #getMethod(String)} to return a {@link Method} object for the named JSON-RPC {@code method}.</li>
 * </ul>
 * <p>
 * The trick to <b>GraalVM</b>-compatibility is to use the {@code static} {@link JsonRpcServiceWrapper#reflect(Class)} method in your
 * implementation at (static) initialization time so the reflection is done at GraalVM compile-time.
 */
public interface JsonRpcServiceWrapper extends JsonRpcService {
    Logger log = LoggerFactory.getLogger(JsonRpcServiceWrapper.class);

    /**
     * Get the service object.
     * <p>Implementations will return their configured service object here.
     *
     * @return the service object
     */
    Object getServiceObject();

    /**
     * Get a {@link Method} object for a named JSON-RPC method
     * @param methodName the name of the method to call
     * @return method handle
     */
    Method getMethod(String methodName);

    /**
     * Handle a request by calling method, getting a result, and embedding it in a response.
     * 
     * @param req The Request POJO
     * @return A future JSON RPC Response
     */
    @Override
    default <RSLT> CompletableFuture<JsonRpcResponse<RSLT>> call(final JsonRpcRequest req) {
        log.debug("JsonRpcServiceWrapper.call: {}", req.getMethod());
        CompletableFuture<RSLT> futureResult = callMethod(req.getMethod(), req.getParams());
        return futureResult.handle((RSLT r, Throwable ex) -> resultCompletionHandler(req, r, ex));
    }
    
    /**
     * Map a request plus a result or error into a response
     *
     * @param req The request being services
     * @param result A result object or null
     * @param ex exception or null
     * @param <RSLT> type of result
     * @return A success or error response as appropriate
     */
    private <RSLT> JsonRpcResponse<RSLT> resultCompletionHandler(JsonRpcRequest req, RSLT result, Throwable ex) {
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
    private JsonRpcError exceptionToError(Throwable ex) {
        if (ex instanceof JsonRpcErrorException) {
            return ((JsonRpcErrorException) ex).getError();
        } else if (ex instanceof JsonRpcException) {
            return JsonRpcError.of(SERVER_EXCEPTION, ex);
        } else {
           return JsonRpcError.of(SERVER_EXCEPTION, ex);
        }
    }

    /**
     * Call an "unwrapped" method from an existing service object
     * 
     * @param methodName Method to call
     * @param params List of JSON-RPC parameters
     * @return A future result POJO
     */
    private <RSLT> CompletableFuture<RSLT> callMethod(String methodName, List<Object> params) {
        log.debug("JsonRpcServiceWrapper.callMethod: {}", methodName);
        CompletableFuture<RSLT> future;
        final Method mh = getMethod(methodName);
        if (mh != null) {
            try {
                @SuppressWarnings("unchecked")
                CompletableFuture<RSLT> liveFuture = (CompletableFuture<RSLT>) mh.invoke(getServiceObject(), params.toArray());
                future = liveFuture;
            } catch (Throwable throwable) {
                log.error("Exception in invoked service object: ", throwable);
                JsonRpcErrorException jsonRpcException = new JsonRpcErrorException(SERVER_EXCEPTION, throwable);
                future = CompletableFuture.failedFuture(jsonRpcException);
            }
        } else {
            future = CompletableFuture.failedFuture(JsonRpcErrorException.of(METHOD_NOT_FOUND));
        }
        return future;
    }

    // TODO: Create a mechanism to return a map with only the desired remotely-accessible methods in it.
    // For server-side JSON-RPC we should migrate away from CompletableFuture to Virtual Threads
    // We should have (or generate) a mapping from lower-case JSON-RPC method names to Java camel-case names,
    // so this `reflect` method can use a List (or Map) data structure and see if Method::name is present
    // in the list.
    /**
     * Use reflection/introspection to generate a map of methods.
     * Generally this is called to initialize a {@link Map} stored in a static field, so the reflection can be done
     * during GraalVM compile-time.
     * @param apiClass The service class to reflect/introspect
     * @return a map of method names to method objects
     */
    static Map<String, Method> reflect(Class<?>  apiClass) {
        return Arrays.stream(apiClass.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))               // Only public methods
                .filter(m -> m.getReturnType().equals(CompletableFuture.class)) // Only methods that return CompletableFuture
                .collect(Collectors
                        .toUnmodifiableMap(Method::getName, // key is method name
                                (method) -> method,         // value is Method object
                                (existingKey, key) -> key)  // if duplicate, replace existing
                );
    }

    /**
     * Wrap a Result POJO in a JsonRpcResponse
     * @param req The request we are responding to
     * @param result the result to wrap
     * @return A valid JsonRpcResponse
     */
    private static <RSLT> JsonRpcResponse<RSLT> wrapResult(JsonRpcRequest req, RSLT result) {
        return new JsonRpcResponse<>(req, result);
    }

    private static <RSLT> JsonRpcResponse<RSLT> wrapError(JsonRpcRequest req, JsonRpcError error) {
        return new JsonRpcResponse<>(req, error);
    }
}
