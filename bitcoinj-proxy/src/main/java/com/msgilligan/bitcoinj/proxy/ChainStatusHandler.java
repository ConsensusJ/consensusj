package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.client.HttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URISyntaxException;

/**
 * Handle a GET request by posting a "getblockchaininfo" and returning the response
 */
@Singleton
public class ChainStatusHandler implements Handler {
    protected static final String jsonType = "application/json";
    private final RPCConfig rpc;
    private final ObjectMapper mapper;

    @Inject
    public ChainStatusHandler(RPCConfig rpcConfig, ObjectMapper objectMapper) {
        rpc = rpcConfig;
        mapper = objectMapper;
    }

    @Override
    public void handle(Context ctx) {
        ctx.get(HttpClient.class).request(rpc.getURI(), requestSpec -> {
            requestSpec.post();
            requestSpec.body(body ->
                    body.type(jsonType).text(buildStatusReq()));
            requestSpec.redirects(0);
            if (rpc.getUsername() != null) {
                requestSpec.basicAuth(rpc.getUsername(), rpc.getPassword());
            }
        }).then(responseStream -> {
            // TODO: Extract from JsonRpcResponse and return more restful JSON format (no RPC wrapper)
            responseStream.forwardTo(ctx.getResponse());
        });
    }

    private String buildStatusReq() {
        JsonRpcRequest req = new JsonRpcRequest("getblockchaininfo");
        return requestToString(req);
    }

    // TODO: Surely this method isn't necessary with Ratpack
    private String requestToString(JsonRpcRequest request) {
        String result;
        try {
            result = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = "proxy jackson error";
        }
        return result;
    }

}
