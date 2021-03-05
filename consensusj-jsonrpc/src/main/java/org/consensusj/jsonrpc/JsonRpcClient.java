package org.consensusj.jsonrpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 *  JSON-RPC client interface. This interface is independent of the JSON conversion library
 *  (the current implementation uses Jackson) and HTTP client library (currently {@link HttpURLConnection}).
 *  For historical reasons the interface is synchronous, but {@link AsyncSupport} makes it easier
 *  to add use of {@link java.util.concurrent.CompletableFuture} for special cases. In the future
 *  this interface may change to natively asynchronous.
 */
public interface JsonRpcClient extends AutoCloseable, AsyncSupport {

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

    /**
     * Call an RPC method and return default object type.
     *
     * Convenience version that takes `params` as array/varargs.
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

    default <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    /**
     * Create a JsonRpcRequest from method and parameters
     *
     * Currently builds JSON-RPC 1.0 request, this method can be overridden for clients
     * that need JSON-RPC 2.0 (e.g. Ethereum)
     *
     * @param method name of method to call
     * @param params parameter Java objects
     * @return A ready-to-send JsonRpcRequest
     */
    default JsonRpcRequest buildJsonRequest(String method, List<Object> params) {
        return new JsonRpcRequest(method, params);
    }

    default JsonRpcRequest buildJsonRequest(String method, Object... params) {
        return new JsonRpcRequest(method, Arrays.asList(params));
    }

    /**
     * Default no-op implementation of close. Classes should override when
     * they have something they need to close properly.
     *
     * @throws IOException if something happens during close
     */
    default void close() throws Exception {
    }
}
