package com.msgilligan.bitcoinj.proxy;

import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.client.HttpClient;

import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Get requests will generate a block (regtest only)
 */
public class GenerateHandler extends AbstractJsonRpcHandler implements Handler {

    public GenerateHandler() throws URISyntaxException {
        super();
    }

    @Override
    public void handle(Context ctx, RPCConfig rpc) {
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
        JsonRpcRequest req = new JsonRpcRequest("generate", Collections.singletonList(1));
        return requestToString(req);
    }

}
