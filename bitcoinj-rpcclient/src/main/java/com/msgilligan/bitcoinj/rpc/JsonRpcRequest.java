package com.msgilligan.bitcoinj.rpc;

import java.util.Collections;
import java.util.List;

/**
 * JSON-RPC Request POJO
 */
public class JsonRpcRequest {
    private static final String JSON_RPC_VERSION = "1.0";
    private static long nextRequestId = 0;

    private final String  jsonrpc;   // version
    private final String  method;
    private final String  id;
    private final List<Object> params;

    public JsonRpcRequest(String method, List<Object> params) {
        this.jsonrpc = JSON_RPC_VERSION;
        this.id =  Long.toString(JsonRpcRequest.nextRequestId++);
        this.method = method;
        if (params != null) {
            // TODO: Should only remove nulls from the end
            params.removeAll(Collections.singleton(null));  // Remove null entries (should only be at end)
        }
        this.params = params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public String getId() {
        return id;
    }

    public List<Object> getParams() {
        return params;
    }
}
