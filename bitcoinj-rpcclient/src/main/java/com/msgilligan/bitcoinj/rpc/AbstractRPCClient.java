package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract Base class for a strongly-typed JSON-RPC client
 */
public abstract class AbstractRPCClient implements UntypedRPCClient {
    protected ObjectMapper mapper;

    public AbstractRPCClient() {
        this.mapper = new ObjectMapper();
    }

    public abstract URI getServerURI();

    /**
     * Subclasses must implement this method to actually send the request
     * @param request
     * @param responseType
     * @param <R>
     * @return
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    protected abstract <R> JsonRpcResponse<R> send(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRPCStatusException;


    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param pass:[<R>] Type of result object
     * @param resultType desired result type as a Java class object
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response = send(request, responseType);

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        return response.getResult();
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, resultType, Arrays.asList(params));
    }


    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param pass:[<R>] Type of result object
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param resultType desired result type as a Jackson JavaType object
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response =  send(request, responseType);

        return response.getResult();
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    /**
     * Call an RPC method and return default object type.
     *
     * Caller should cast returned object to the correct type.
     *
     * Useful for:
     * * Simple (not client-side validated) command line utilities
     * * Functional tests that need to send incorrect types to the server to test error handling
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param pass:[<R>] Type of result object
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    @Override
    public <R> R send(String method, List<Object> params) throws IOException, JsonRPCStatusException {
        return (R) send(method, (Class<R>) Object.class, params);
    }

    /**
     * Call an RPC method and return default object type.
     *
     * Caller should cast returned object to the correct type.
     *
     * Useful for:
     * * Simple (not client-side validated) command line utilities
     * * Functional tests that need to send incorrect types to the server to test error handling
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param pass:[<R>] Type of result object
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    @Override
    public <R> R send(String method, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, Arrays.asList(params));
    }

}
