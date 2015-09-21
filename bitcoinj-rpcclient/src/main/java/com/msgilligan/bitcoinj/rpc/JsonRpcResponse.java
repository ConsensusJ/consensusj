package com.msgilligan.bitcoinj.rpc;

/**
 * JSON-RPC Response POJO
 */
public class JsonRpcResponse {
    private String          jsonrpc;   // version
    private Object          result;
    private JsonRpcError    error;
    private String          id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
