package org.consensusj.proxy;

import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.ratpack.JsonRpcClient;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static ratpack.jackson.Jackson.json;

/**
 * Get requests will generate a block (regtest only)
 */
@Singleton
public class GenerateHandler implements Handler {
    private final JsonRpcClient rpcClient;

    @Inject
    public GenerateHandler(JsonRpcClient jsonRpcClient) {
        rpcClient = jsonRpcClient;
    }

    @Override
    public void handle(Context ctx) {
        JsonRpcRequest rpcReq = new JsonRpcRequest("setgenerate", Arrays.asList(true, 1));
        rpcClient.call(rpcReq).then(rpcResponse -> ctx.render(json(rpcResponse)));
    }
}
