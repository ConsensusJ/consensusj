package org.consensusj.jsonrpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  JSON-RPC client interface. This interface is independent of the JSON conversion library
 *  (the default implementation uses Jackson) and HTTP client library (currently {@link HttpURLConnection}).
 *  For historical reasons the interface is synchronous, but {@link AsyncSupport} makes it easier
 *  to add use of {@link java.util.concurrent.CompletableFuture} for special cases. In the future
 *  this interface may change to natively asynchronous.
 *  <p>
 *  Both JSON-RPC 1.0 and JSON-RPC 2.0 are supported. Implementations should also be (via configuration, perhaps)
 *  lenient enough to support Bitcoin Core and similar servers that don't follow the JSON-RPC specifications exactly.
 * @see <a href="https://www.jsonrpc.org/specification_v1">JSON-RPC 1.0 Specification (2005)</a>
 * @see <a href="https://www.jsonrpc.org/specification">JSON-RPC 2.0 Specification</a>
 */
public interface JsonRpcClient extends AutoCloseable, AsyncSupport {

    /**
     * Return the JSON-RPC version used by the implementation
     *
     * @return JSON-RPC version
     */
    JsonRpcMessage.Version getJsonRpcVersion();

    /**
     * Get the URI of the remote server
     * @return URI of remote server
     */
    URI getServerURI();
    
    /**
     * Call an RPC method and return "default" object type. Caller should cast returned object to the correct type.
     * <p>
     * The parameter list is "untyped" (declared as {@code List<Object>}) and implementations are responsible
     * for converting each Java object parameter to a valid and correctly-typed (for {@code method}) JSON object.
     * <p>
     * This is used to implement the {@code DynamicRpcMethodFallback} trait in Groovy which is applied
     * to various Groovy RPC client implementations that typically inherit statically-dispatched
     * methods from Java classes, but use {@code methodMissing()} to add JSON-RPC methods dynamically.
     * This may be useful in other Dynamic JVM languages, as well.
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC parameters using types that are convertible to JSON
     * @param <R> Type of result object
     * @return the `response.result` field of the JSON-RPC response cast to type R
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    <R> R send(String method, List<Object> params) throws IOException, JsonRpcStatusException;

    <R> CompletableFuture<R> sendAsync(String method, List<Object> params);

    /**
     * Call an RPC method and return default object type.
     * <p>
     * Convenience version that takes {@code params} as array/varargs.
     *
     * @param <R> Type of result object
     * @param method JSON RPC method call to send
     * @param params JSON RPC parameters as array or varargs
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    default <R> R send(String method, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, Arrays.asList(params));
    }

    <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRpcStatusException;

    <R> CompletableFuture<R> sendAsync(String method, Class<R> resultType, List<Object> params);

    default <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    default <R> CompletableFuture<R> sendAsync(String method, Class<R> resultType, Object... params) {
        return sendAsync(method, resultType, Arrays.asList(params));
    }

    /**
     * Synchronously complete a JSON-RPC request by calling {@link CompletableFuture#get()}, unwrapping nested
     * {@link JsonRpcException} or {@link IOException} from {@link ExecutionException}.
     * @param future The {@code CompletableFuture} (result of JSON-RPC request) to unwrap
     * @return A JSON-RPC result
     * @param <R> The expected result type
     * @throws IOException If {@link CompletableFuture#get} threw  {@code ExecutionException} caused by {@code IOException}
     * @throws JsonRpcException If {@link CompletableFuture#get} threw  {@code ExecutionException} caused by {@code JsonRpcException}
     * @throws RuntimeException If {@link CompletableFuture#get} threw {@link InterruptedException} or other {@link ExecutionException}.
     */
    default <R> R syncGet(CompletableFuture<R> future) throws IOException, JsonRpcException {
        try {
            return future.get();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof JsonRpcException) {
                throw (JsonRpcException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(ee);
            }
        }
    }

    /**
     * Create a JsonRpcRequest from method and parameters
     *
     * @param method name of method to call
     * @param params parameter Java objects
     * @return A ready-to-send JsonRpcRequest
     */
    default JsonRpcRequest buildJsonRequest(String method, List<Object> params) {
        return new JsonRpcRequest(getJsonRpcVersion(), method, params);
    }

    default JsonRpcRequest buildJsonRequest(String method, Object... params) {
        return new JsonRpcRequest(getJsonRpcVersion(), method, Arrays.asList(params));
    }

    /**
     * Default no-op implementation of {@link AutoCloseable#close()}. Classes should override when
     * they have something they need to close properly.
     *
     * @throws IOException if something happens during close
     */
    @Override
    default void close() throws Exception {
    }
}
