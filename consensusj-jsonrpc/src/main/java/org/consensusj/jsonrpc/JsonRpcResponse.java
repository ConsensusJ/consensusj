package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.consensusj.jsonrpc.internal.NumberStringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON-RPC Response POJO
 *
 * Note that `result` is a parameterized type and can be used to directly map JSON-RPC results
 * to the correct type for each method.
 */
public class JsonRpcResponse<R> {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcResponse.class);
    private final String          jsonrpc;   // version
    private final String          id;
    private final R               result;
    private final JsonRpcError    error;

    @JsonCreator
    public JsonRpcResponse(@JsonProperty("jsonrpc")  String jsonrpc,
                            @JsonProperty("id")      String id,
                            @JsonProperty("result")  R result,
                            @JsonProperty("error")   JsonRpcError error) {
        if ((result == null && error == null) || (result != null && error != null)) {
            log.warn("non-compliant response: (error, result) both null or both set.");
        }
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
        this.error = error;
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
