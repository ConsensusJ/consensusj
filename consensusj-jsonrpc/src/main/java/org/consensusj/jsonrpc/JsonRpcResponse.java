package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC Response POJO
 *
 * Note that `result` is a parameterized type and can be used to directly map JSON-RPC results
 * to the correct type for each method.
 */
public class JsonRpcResponse<R> {
    private final R               result;
    private final JsonRpcError    error;
    private final String          jsonrpc;   // version
    private final String          id;

    @JsonCreator
    public JsonRpcResponse(@JsonProperty("result")  R result,
                           @JsonProperty("error")   JsonRpcError error,
                           @JsonProperty("jsonrpc") String jsonrpc,
                           @JsonProperty("id")      String id) {
        this.jsonrpc = jsonrpc;
        this.result = result;
        this.error = error;
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public R getResult() {
        return result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public String getId() {
        return id;
    }


    // TODO: Add .toString() method for logging
}
