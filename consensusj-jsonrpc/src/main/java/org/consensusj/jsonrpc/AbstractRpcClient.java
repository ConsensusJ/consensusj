package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Abstract Base class for a strongly-typed JSON-RPC client. This abstract class handles
 * the use of Jackson to map from JSON to Java Objects, but leaves the core {@code sendRequestForResponse} method as
 * {@code abstract} to be implemented by subclasses allowing implementation with alternative
 * HTTP client libraries.
 * Note: We may be able to pull more <i>or all</i> of this functionality into {@link JacksonRpcClient}
 */
public abstract class AbstractRpcClient implements JsonRpcClient, JacksonRpcClient {
    protected final ObjectMapper mapper;
    private final JavaType defaultType;

    public AbstractRpcClient() {
        mapper = new ObjectMapper();
        // TODO: Provide external API to configure FAIL_ON_UNKNOWN_PROPERTIES
        // TODO: Remove "ignore unknown" annotations on various POJOs that we've defined.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultType = mapper.getTypeFactory().constructType(Object.class);
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Subclasses must implement this method to actually send the request
     * @param <R> Type of result object
     * @param request The request to send
     * @param responseType The response to expected (used by Jackson for conversion)
     * @return A JSON RPC Response with `result` of type `R`
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    public abstract <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException;


    private <R> R sendRequestForResult(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        JsonRpcResponse<R> response = sendRequestForResponse(request, responseType);

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
    public <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
//        JavaType responseType = mapper.getTypeFactory().
//                constructParametricType(JsonRpcResponse.class, resultType);
        return sendRequestForResult(buildJsonRequest(method, params), responseTypeFor(resultType));
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
    protected <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        //JavaType responseType = mapper.getTypeFactory().
        //        constructParametricType(JsonRpcResponse.class, resultType);
        return sendRequestForResult(buildJsonRequest(method, params), responseTypeFor(resultType));
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRpcStatusException {
        return sendRequestForResult(buildJsonRequest(method, params), responseTypeFor(resultType));
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
    public <R> R send(String method, List<Object> params) throws IOException, JsonRpcStatusException {
        return send(method, defaultType, params);
    }
}
