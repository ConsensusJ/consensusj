package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.fromJson;
import ratpack.http.client.HttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * JsonRPC proxy handler
 * Relay allowed RPC methods to a URI
 */
@Singleton
public class RpcProxyHandler implements Handler {
    protected static final String jsonType = "application/json";
    private final List<String> allowedMethods =  Arrays.asList("getblockcount", "setgenerate");
    private final RPCConfig rpc;
    private final ObjectMapper mapper;

    @Inject
    public RpcProxyHandler(RPCConfig rpcConfig, ObjectMapper objectMapper) {
        rpc = rpcConfig;
        mapper = objectMapper;
    }

    @Override
    public void handle(Context ctx)  {
        ctx.parse(fromJson(JsonRpcRequest.class)).then(rpcReq -> {
            if (allowedMethods.contains(rpcReq.getMethod())) {
                ctx.get(HttpClient.class).requestStream(rpc.getURI(), requestSpec -> {
                    requestSpec.post();
                    requestSpec.body(body ->
                            body.type(jsonType).text(requestToString(rpcReq)));
                    requestSpec.redirects(0);
                    if (rpc.getUsername() != null) {
                        requestSpec.basicAuth(rpc.getUsername(), rpc.getPassword());
                    }
                }).then(responseStream -> {
                    responseStream.forwardTo(ctx.getResponse());
                });
            } else {
                // Should we send a JsonRpcResponse here?
                ctx.getResponse().status(403).send("JSON-RPC method not allowed by proxy");
            }
        });
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
