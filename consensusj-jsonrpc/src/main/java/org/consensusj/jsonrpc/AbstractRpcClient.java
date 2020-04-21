package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract Base class for a strongly-typed JSON-RPC client. This abstract class handles
 * the use of Jackson to map from JSON to Java, but leaves the core `send` method as
 * `abstract` to be implemented by subclasses allowing implementation with alternative
 * HTTP client libraries.
 */
public abstract class AbstractRpcClient implements DynamicRpcMethodSupport {
    protected final ObjectMapper mapper;
    private final JavaType defaultType;

    public AbstractRpcClient() {
        mapper = new ObjectMapper();
        // TODO: Make  FAIL_ON_UNKNOWN_PROPERTIES configurable at build or run time and remove unnecessary
        // annotations on various POJOs that we've defined.
        //mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        defaultType = mapper.getTypeFactory().constructType(Object.class);
    }

    public abstract URI getServerURI();

    /**
     * Subclasses must implement this method to actually send the request
     * @param pass:[<R>] Type of result object
     * @param request The request to send
     * @param responseType The response to expected (used by Jackson for conversion)
     * @return A JSON RPC Response with `result` of type `R`
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    protected abstract <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException;

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
    protected JsonRpcRequest buildJsonRequest(String method, List<Object> params) {
        return new JsonRpcRequest(method, params);
    }

    private <R> R sendForResult(String method, JavaType responseType, List<Object> params) throws IOException, JsonRpcStatusException {
        JsonRpcRequest request = buildJsonRequest(method, params);
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
     * @param pass:[<R>] Type of result object
     * @param method JSON RPC method call to send
     * @param resultType desired result type as a Java class object
     * @param params JSON RPC params
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
        return sendForResult(method, responseType, params);
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, resultType, Arrays.asList(params));
    }


    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param pass:[<R>] Type of result object
     * @param method JSON RPC method call to send
     * @param resultType desired result type as a Jackson JavaType object
     * @param params JSON RPC params
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRpcStatusException {
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
        return sendForResult(method, responseType, params);
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, resultType, Arrays.asList(params));
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
     * @param pass:[<R>] Type of result object
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

    /**
     * Call an RPC method and return default object type.
     *
     * Convenience version that takes `params` as array/varargs.
     *
     * @param pass:[<R>] Type of result object
     * @param method JSON RPC method call to send
     * @param params JSON RPC parameters as array or varargs
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    public <R> R send(String method, Object... params) throws IOException, JsonRpcStatusException {
        return send(method, Arrays.asList(params));
    }

}
