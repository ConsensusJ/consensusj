package com.msgilligan.bitcoinj.proxy;

import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.ratpack.JsonRpcClient;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ratpack.jackson.Jackson.json;

/**
 * Handle a GET request by posting a "getblockchaininfo" and returning the response
 */
@Singleton
public class ChainStatusHandler implements Handler {
    private final JsonRpcClient rpcClient;

    @Inject
    public ChainStatusHandler(JsonRpcClient jsonRpcClient) {
        rpcClient = jsonRpcClient;
    }

    @Override
    public void handle(Context ctx) {
        JsonRpcRequest rpcReq = new JsonRpcRequest("getblockchaininfo");
        rpcClient.call(rpcReq).then(rpcResponse -> ctx.render(json(rpcResponse)));
    }
}
