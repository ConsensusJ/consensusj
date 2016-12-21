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
import java.util.Arrays;
import java.util.Collections;

/**
 * Get requests will generate a block (regtest only)
 */
@Singleton
public class GenerateHandler implements Handler {
    protected static final String jsonType = "application/json";

    private final RPCConfig rpc;
    private final ObjectMapper mapper;

    @Inject
    public GenerateHandler(RPCConfig rpcConfig, ObjectMapper objectMapper) {
        rpc = rpcConfig;
        mapper = objectMapper;
    }

    @Override
    public void handle(Context ctx) {
        ctx.get(HttpClient.class).requestStream(rpc.getURI(), requestSpec -> {
            requestSpec.post();
            requestSpec.body(body ->
                    body.type(jsonType).text(buildGenReq()));
            requestSpec.redirects(0);
            if (rpc.getUsername() != null) {
                requestSpec.basicAuth(rpc.getUsername(), rpc.getPassword());
            }
        }).then(responseStream -> {
            // TODO: Extract from JsonRpcResponse and return more restful JSON format (no RPC wrapper)
            responseStream.forwardTo(ctx.getResponse());
        });
    }

    private String buildGenReq() {
//        JsonRpcRequest req = new JsonRpcRequest("generate", Collections.singletonList(1));
        JsonRpcRequest req = new JsonRpcRequest("setgenerate", Arrays.asList(true, 1));
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
