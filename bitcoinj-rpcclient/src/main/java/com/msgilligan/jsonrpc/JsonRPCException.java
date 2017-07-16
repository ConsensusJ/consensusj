package com.msgilligan.jsonrpc;

/**
 * JSON RPC Exception
 */
public class JsonRPCException extends Exception {

    public JsonRPCException(String message) {
        super(message);
    }

    public JsonRPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
