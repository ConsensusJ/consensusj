package com.msgilligan.bitcoinj.proxy;

import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import static ratpack.jackson.Jackson.fromJson;
import ratpack.http.client.HttpClient;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * JsonRPC proxy handler
 * Relay allowed RPC methods to a URI
 * (defaults to localhost with regtest port)
 */
public class RpcProxyHandler extends AbstractJsonRpcHandler implements Handler {
    private final List<String> allowedMethods =  Arrays.asList("getblockcount", "setgenerate");

    public RpcProxyHandler() throws URISyntaxException {
        super();
    }


    @Override
    public void handle(Context ctx, RPCConfig rpc)  {
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

}
