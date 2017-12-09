package org.consensusj.jsonrpc.ratpack;

import org.consensusj.jsonrpc.JsonRpcRequest;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

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
    private final List<String> allowedMethods =  Arrays.asList("getblockcount", "setgenerate");
    private final JsonRpcClient rpcClient;

    @Inject
    public RpcProxyHandler(JsonRpcClient jsonRpcClient) {
        rpcClient = jsonRpcClient;
    }

    @Override
    public void handle(Context ctx)  {
        ctx.parse(fromJson(JsonRpcRequest.class)).then(rpcReq -> {
            if (allowedMethods.contains(rpcReq.getMethod())) {
                rpcClient.call(rpcReq).then(rpcResponse -> ctx.render(json(rpcResponse)));
            } else {
                // Should we send a JsonRpcResponse here?
                ctx.getResponse().status(403).send("JSON-RPC method not allowed by proxy");
            }
        });
    }
}
