package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC Error Object POJO
 */
public class JsonRpcError {
    private final int code;
    private final String message;
    private final Object data;


    @JsonCreator
    public JsonRpcError(@JsonProperty("code") int code,
                        @JsonProperty("message") String message,
                        @JsonProperty("data") Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
}
