package org.consensusj.jsonrpc;

import java.io.IOException;

/**
 * JSON RPC Exception
 */
public class JsonRpcException extends IOException {

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
