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
