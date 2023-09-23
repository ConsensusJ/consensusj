package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

// TODO: Step 1: Create interface in JsonRpcClient replacing JavaType with java.lang.reflect.Type
// TODO: Step 2: Eliminate JacksonRpcClient by pulling up or pushing down all methods
// TODO: Step 3: Migrate JSON mapping function to component "JsonRpcTypeProcessor" class with Jackson implementation
/**
 * Interface with default methods for a strongly-typed JSON-RPC client that uses Jackson to map from JSON to Java Objects.
 */
public interface JacksonRpcClient extends JsonRpcClient {

    ObjectMapper getMapper();

    JavaType getDefaultType();

    default JavaType responseTypeFor(JavaType resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }

    default JavaType responseTypeFor(Class<?> resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }

    default JavaType typeForClass(Class<?> clazz) {
        return getMapper().constructType(clazz);
    }

    /**
     * Send a {@link JsonRpcRequest} for a {@link JsonRpcResponse}
     * <p>Synchronous subclasses should override this method to prevent {@link CompletableFuture#supplyAsync(Supplier)} from
     * being called twice when {@link AsyncSupport} is being used. Eventually we'll migrate more of the codebase to native
     * async, and then we won't have to worry about calling {@code supplyAsync} twice.
     * @param <R> Type of result object
     * @param request The request to send
     * @param responseType The response type expected (used by Jackson for conversion)
     * @return A JSON RPC Response with `result` of type `R`
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    default <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResponseAsync(request, responseType));
    }

    /**
     * Send a {@link JsonRpcRequest} for a {@link JsonRpcResponse} asynchronously.
     * @param <R> Type of result object
     * @param request The request to send
     * @param responseType The response type expected (used by Jackson for conversion)
     * @return A future JSON RPC Response with `result` of type `R`
     */
    <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType);

    /**
     * Convenience method for requesting a response with a {@link JsonNode} for the result.
     * @param request The request to send
     * @return A JSON RPC Response with `result` of type {@code JsonNode}
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    default JsonRpcResponse<JsonNode> sendRequestForResponse(JsonRpcRequest request) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResponseAsync(request));
    }

    /**
     * Convenience method for requesting an asynchronous response with a {@link JsonNode} for the result.
     * @param request The request to send
     * @return A future JSON RPC Response with `result` of type {@code JsonNode}
     */
    default CompletableFuture<JsonRpcResponse<JsonNode>> sendRequestForResponseAsync(JsonRpcRequest request) {
        return sendRequestForResponseAsync(request, responseTypeFor(JsonNode.class));
    }

    private <R> CompletableFuture<R> sendRequestForResultAsync(JsonRpcRequest request, JavaType resultType) {
        CompletableFuture<JsonRpcResponse<R>> responseFuture = sendRequestForResponseAsync(request, responseTypeFor(resultType));

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        // TODO: Error case should probably complete with JsonRpcErrorException (not status exception with code 200)
        return responseFuture.thenCompose(resp -> (resp.getError() == null || resp.getError().getCode() == 0)
                    ? CompletableFuture.completedFuture(resp.getResult())
                    : CompletableFuture.failedFuture(new JsonRpcStatusException(
                                resp.getError().getMessage(),
                                200,    // If response code wasn't 200 we couldn't be here
                                null,
                                resp.getError().getCode(),
                                null,
                                resp))
        );
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
    @Override
    default <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), typeForClass(resultType)));
    }

    default <R> CompletableFuture<R> sendAsync(String method, Class<R> resultType, List<Object> params) {
        return sendRequestForResultAsync(buildJsonRequest(method, params), typeForClass(resultType));
    }

    default <R> CompletableFuture<R> sendAsync(String method, JavaType resultType, List<Object> params) {
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
    default <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), resultType));
    }

    /**
     * Varargs version
     */
    default <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResultAsync(buildJsonRequest(method, params), resultType));
    }

    default <R> CompletableFuture<R> sendAsync(String method, JavaType resultType, Object... params) {
        return sendRequestForResultAsync(buildJsonRequest(method, params), resultType);
    }

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
    @Override
    default <R> R send(String method, List<Object> params) throws IOException, JsonRpcStatusException {
        return send(method, getDefaultType(), params);
    }

    @Override
    default <R> CompletableFuture<R>  sendAsync(String method, List<Object> params) {
        return sendAsync(method, getDefaultType(), params);
    }
}
