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

    private JsonRpcError(Error code) {
        this.code = code.code;
        this.message = code.message;
        this.data = null;
    }

    private JsonRpcError(Error code, Throwable throwable) {
        this.code = code.code;
        this.message = code.message + ": " + throwable.getMessage();
        this.data = null;
    }

    public static JsonRpcError of(Error code) {
        return new JsonRpcError(code);
    }

    public static JsonRpcError of(Error code, Throwable throwable) {
        return new JsonRpcError(code, throwable);
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

    public enum Error {
        // TODO: Add other reserved error codes from the spec
        METHOD_NOT_FOUND(-32601, "Method not found"),
        SERVER_ERROR(-32000, "Server error"),
        SERVER_EXCEPTION(-32001, "Server exception");

        private final int code;
        private final String message;

        Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
