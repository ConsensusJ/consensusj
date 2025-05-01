package org.consensusj.jsonrpc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  JSON-RPC client interface. This interface is independent of the JSON conversion library
 *  (the default implementation uses Jackson) and HTTP client library (currently {@link HttpURLConnection}).
 *  For historical reasons the interface is mostly synchronous, but {@link AsyncSupport} makes it easier
 *  to add use of {@link java.util.concurrent.CompletableFuture} for special cases. In the future
 *  this interface may change to natively asynchronous.
 *  <p>
 *  Both JSON-RPC 1.0 and JSON-RPC 2.0 are supported. Implementations should also be (via configuration, perhaps)
 *  lenient enough to support Bitcoin Core and similar servers that don't follow the JSON-RPC specifications exactly.
 * @see <a href="https://www.jsonrpc.org/specification_v1">JSON-RPC 1.0 Specification (2005)</a>
 * @see <a href="https://www.jsonrpc.org/specification">JSON-RPC 2.0 Specification</a>
 */
public interface JsonRpcClient<T extends Type> extends JsonRpcTransport<T>, AutoCloseable {

    /**
     * Return the JSON-RPC version used by the implementation
     *
     * @return JSON-RPC version
     */
    JsonRpcMessage.Version getJsonRpcVersion();

    /**
     * Call an RPC method and return default object type.
     * <p>
     * Caller should cast returned object to the correct type.
     * <p>
     * Useful for:
     * <ul>
     * <li>Dynamically-dispatched JSON-RPC methods calls via Groovy subclasses</li>
     * <li>Simple (not client-side validated) command line utilities</li>
     * <li>Functional tests that need to send incorrect types to the server to test error handling</li>
     * </ul>
     *
     * @param <R> Type of result object
     * @param method JSON RPC method call to send
     * @param params JSON RPC parameters as a `List`
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    default <R> R send(String method, List<Object> params) throws IOException, JsonRpcStatusException {
        return send(method, defaultType(), params);
    }

    default <R> CompletableFuture<R>  sendAsync(String method, List<Object> params) {
        return sendAsync(method, defaultType(), params);
    }

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

    default <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    default <R> CompletableFuture<R> sendAsync(String method, Class<R> resultType, Object... params) {
        return sendAsync(method, resultType, Arrays.asList(params));
    }

    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param <R> Type of result object
     * @param method JSON RPC method call to send
     * @param resultType desired result type as a Java class object
     * @param params JSON RPC params
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    default <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), typeForClass(resultType)));
    }

    default <R> CompletableFuture<R> sendAsync(String method, Class<R> resultType, List<Object> params) {
        return sendRequestForResultAsync(buildJsonRequest(method, params), typeForClass(resultType));
    }

    default <R> CompletableFuture<R> sendAsync(String method, T resultType, List<Object> params) {
        return sendRequestForResultAsync(buildJsonRequest(method, params), resultType);
    }

    /**
     * JSON-RPC remote method call that returns {@code response.result}
     *
     * @param <R> Type of result object
     * @param method JSON RPC method call to send
     * @param resultType desired result type as a Jackson JavaType object
     * @param params JSON RPC params
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    default <R> R send(String method, T resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), resultType));
    }

    /**
     * Varargs version
     */
    default <R> R send(String method, T resultType, Object... params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), resultType));
    }

    default <R> CompletableFuture<R> sendAsync(String method, T resultType, Object... params) {
        return sendRequestForResultAsync(buildJsonRequest(method, params), resultType);
    }

    private <R> CompletableFuture<R> sendRequestForResultAsync(JsonRpcRequest request, T resultType) {
        CompletableFuture<JsonRpcResponse<R>> responseFuture = sendRequestForResponseAsync(request, responseTypeFor(resultType));

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        // TODO: Error case should probably complete with JsonRpcErrorException (not status exception with code 200)
        return responseFuture.thenCompose(resp -> (resp.getError() == null || resp.getError().getCode() == 0)
                ? CompletableFuture.completedFuture(resp.getResult())
                : CompletableFuture.failedFuture(new JsonRpcStatusException(200, resp)) // If response code wasn't 200 we couldn't be here
        );
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
     * Default no-op implementation of {@link AutoCloseable#close()}. Subclasses can override if
     * they have something they need to close properly.
     */
    @Override
    default void close() {}

    T defaultType();

    T responseTypeFor(T resultType);
    T responseTypeFor(Class<?> resultType);

    T typeForClass(Class<?> clazz);

    T collectionTypeForClasses(Class<? extends Collection> collectionClazz, Class<?> clazz);

    T collectionTypeForClasses(Class<? extends Collection> collectionClazz, T itemType);
}
