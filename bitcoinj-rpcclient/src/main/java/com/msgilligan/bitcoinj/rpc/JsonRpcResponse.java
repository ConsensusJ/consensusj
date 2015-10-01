package com.msgilligan.bitcoinj.rpc;

/**
 * = JSON-RPC Response POJO
 *
 * Note that `result` is a parameterized type and can be used to directly map JSON-RPC results
 * to the correct type for each method.
 */
public class JsonRpcResponse<R> {
    private String          jsonrpc;   // version
    private R               result;
    private JsonRpcError    error;
    private String          id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
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

    // TODO: Add .toString() method for logging
}
