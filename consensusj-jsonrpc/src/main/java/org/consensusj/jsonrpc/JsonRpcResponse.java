package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.NullNode;
import org.consensusj.jsonrpc.internal.NumberStringSerializer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON-RPC Response POJO
 * <p>
 * Note that {@code result} is a parameterized type and can be used to directly map JSON-RPC results
 * to the correct type for each method.
 * @param <RSLT> The type of the JSON-RPC {@code result}.
 */
public class JsonRpcResponse<RSLT> {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcResponse.class);
    private final String          jsonrpc;   // version
    private final String          id;
    @Nullable
    private final RSLT            result;
    @Nullable
    private final JsonRpcError    error;

    @JsonCreator
    public JsonRpcResponse(@JsonProperty("jsonrpc")  String jsonrpc,
                            @JsonProperty("id")      String id,
                            @Nullable @JsonProperty("result")  RSLT result,
                            @Nullable @JsonProperty("error")   JsonRpcError error) {
        if ((error == null && result == null) ||
            (error != null && result != null && !(result instanceof NullNode))) {
            log.warn("non-compliant response: (error, result) both null or both set.");
        }
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
        this.error = error;
    }

    public JsonRpcResponse(JsonRpcRequest request,
                           RSLT result) {
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
    @Nullable
    public RSLT getResult() {
        return result;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    public JsonRpcError getError() {
        return error;
    }

    @JsonSerialize(using= NumberStringSerializer.class)
    public String getId() {
        return id;
    }

    // TODO: Add .toString() method for logging
}
