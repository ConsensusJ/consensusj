package org.consensusj.jsonrpc;

import java.io.IOException;

/**
 * JSON RPC Exception
 * <p>
 * The {@link JsonRpcErrorException} subclass includes a {@link JsonRpcError} object which can be used
 * in server implementations to pass error information to the server's response serialization layer which
 * can include the {@link JsonRpcError} in the {@link JsonRpcResponse} and in client implementations it can
 * be used for a client to find the {@link JsonRpcError} that was returned.
 * <p>
 * The {@link JsonRpcStatusException} subclass contains support for HTTP response code and message.
 * <p>
 * TODO: Rethink the differences between the two subclasses in light of Bitcoin (and possibly other)
 * implementations returning HTTP status codes along with JsonRpcError responses. Bitcoin even incorrectly
 * returns a 500 for invalid parameters. I think I originally assume that their would be either an
 * HTTP status error or a JSON-RPC error, but in reality responses can probably have neither, either, or both.
 * @see <a href="https://github.com/bitcoin/bitcoin/issues/2960">Bitcoin Core Issue #2960</a>
 */
public class JsonRpcException extends IOException {

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
