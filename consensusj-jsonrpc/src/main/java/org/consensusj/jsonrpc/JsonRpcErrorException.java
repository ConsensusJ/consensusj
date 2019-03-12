package org.consensusj.jsonrpc;

/**
 *  Exception wrapper for JsonRpcError
 *  Useful in server implementations, throwing an error that will be transmitted
 *  to the client.
 */
public class JsonRpcErrorException extends JsonRpcException {
    private JsonRpcError error;

    public JsonRpcErrorException(JsonRpcError error) {
        super(error.getMessage());
        this.error = error;
    }

    public JsonRpcErrorException(JsonRpcError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }

    public JsonRpcErrorException(JsonRpcError.Error code, Throwable cause) {
        super(code.getMessage(), cause);
        this.error = JsonRpcError.of(code);
    }

    /**
     * Get the JSON RPC Error POJO
     * @return An error object
     */
    public JsonRpcError getError() {
        return error;
    }

    public static JsonRpcErrorException of(JsonRpcError.Error code) {
        return new JsonRpcErrorException(JsonRpcError.of(code));
    }
}
