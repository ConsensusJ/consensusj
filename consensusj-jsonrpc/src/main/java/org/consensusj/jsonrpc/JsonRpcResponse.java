package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.consensusj.jsonrpc.internal.NumberStringSerializer;

import java.util.Optional;

import static org.consensusj.jsonrpc.JsonRpcMessage.Version.*;

/**
 * JSON-RPC Response POJO
 *
 * Note that `result` is a parameterized type and can be used to directly map JSON-RPC results
 * to the correct type for each method.
 */
public class JsonRpcResponse<R> {
    private final String          jsonrpc;   // version
    private final String          id;
    private final R               result;
    private final JsonRpcError    error;

    @JsonCreator
    public JsonRpcResponse(@JsonProperty("jsonrpc")  String jsonrpc,
                            @JsonProperty("id")      String id,
                            @JsonProperty("result")  R result,
                            @JsonProperty("error")   JsonRpcError error) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
        this.error = error;
    }

    @Deprecated
    public JsonRpcResponse(R result,
                           JsonRpcError error,
                           String jsonrpc,
                           String id) {
        this.jsonrpc = jsonrpc;
        this.result = result;
        this.error = error;
        this.id = id;
    }

    public JsonRpcResponse(JsonRpcRequest request,
                           R result) {
        this.jsonrpc = request.getJsonrpc();
        this.id = request.getId();
        this.result = result;
        this.error = null;
    }

    public JsonRpcResponse(JsonRpcRequest request,
                           JsonRpcError error) {
        this.jsonrpc = request.getJsonrpc();
        this.id = request.getId();
        this.error = error;
        this.result = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getJsonrpc() {
        return jsonrpc;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public R getResult() {
        return result;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public JsonRpcError getError() {
        return error;
    }

    @JsonSerialize(using= NumberStringSerializer.class)
    public String getId() {
        return id;
    }

    // TODO: Add .toString() method for logging
}
