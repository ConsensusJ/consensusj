package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
        try {
            return (JsonRpcResponse<R>) sendRequestForResponseAsync(request, responseType).get();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof JsonRpcStatusException) {
                throw (JsonRpcStatusException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(ee);
            }
        }
    }

    /**
     * Send a {@link JsonRpcRequest} for a {@link JsonRpcResponse} asynchronously.
     * <p>Subclasses must implement this method to actually send the request
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
        return sendRequestForResponse(request, responseTypeFor(JsonNode.class));
    }

    private <R> R sendRequestForResult(JsonRpcRequest request, JavaType resultType) throws IOException, JsonRpcStatusException {
        JsonRpcResponse<R> response = sendRequestForResponse(request, responseTypeFor(resultType));

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        if (response.getError() != null && response.getError().getCode() != 0) {
            throw new JsonRpcStatusException(
                    response.getError().getMessage(),
                    200,    // If response code wasn't 200 we couldn't be here
                    null,
                    response.getError().getCode(),
                    null,
                    response);
        }
        return response.getResult();
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
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
//        JavaType responseType = mapper.getTypeFactory().
//                constructParametricType(JsonRpcResponse.class, resultType);
        return sendRequestForResult(buildJsonRequest(method, params), typeForClass(resultType));
    }

    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param <R> Type of result object
     * @param method JSON RPC method call to send
     * @param resultType desired result type as a Jackson JavaType object
     * @param params JSON RPC params
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    default <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        //JavaType responseType = mapper.getTypeFactory().
        //        constructParametricType(JsonRpcResponse.class, resultType);
        return sendRequestForResult(buildJsonRequest(method, params), resultType);
    }

    /**
     * Varargs version
     */
    default <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRpcStatusException {
        return sendRequestForResult(buildJsonRequest(method, params), resultType);
    }

    /**
     * Call an RPC method and return default object type.
     *
     * Caller should cast returned object to the correct type.
     *
     * Useful for:
     * * Dynamically-dispatched JSON-RPC methods calls via Groovy subclasses
     * * Simple (not client-side validated) command line utilities
     * * Functional tests that need to send incorrect types to the server to test error handling
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
}
