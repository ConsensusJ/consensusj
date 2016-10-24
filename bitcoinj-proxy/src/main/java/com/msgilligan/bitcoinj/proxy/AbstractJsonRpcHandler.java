package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.handling.Context;
import ratpack.handling.InjectionHandler;

/**
 * Base class for JSON RPC handlers (until we have a better solution for config and common code)
 */
public abstract class AbstractJsonRpcHandler extends InjectionHandler {
    protected static final String jsonType = "application/json";
    protected final ObjectMapper mapper = new ObjectMapper();

    abstract public void handle(Context ctx, RPCConfig rpc);

    // TODO: Surely this method isn't necessary with Ratpack
    protected String requestToString(JsonRpcRequest request) {
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
