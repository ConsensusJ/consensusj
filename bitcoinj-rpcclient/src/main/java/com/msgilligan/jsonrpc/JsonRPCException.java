package com.msgilligan.jsonrpc;

import java.io.IOException;

/**
 * JSON RPC Exception
 */
public class JsonRPCException extends IOException {

    public JsonRPCException(String message) {
        super(message);
    }

    public JsonRPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
