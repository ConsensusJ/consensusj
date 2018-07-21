package org.consensusj.jsonrpc;

/**
 * JSON-RPC returned HTTP status other than 200
 * Additional information is usually in JSON-RPC response
 */
public class JsonRpcStatusException extends JsonRpcException {
    public final String httpMessage;
    public final int httpCode;
    public final int jsonRPCCode;
    public final String response;
    public final JsonRpcResponse responseJson;

    /**
     * Default Constructor
     *
     * @param message Error message from Json if available, else http status message
     * @param httpCode HTTP status code, e.g. 404
     * @param httpMessage HTTP status message, e.g. "Not found"
     * @param jsonRPCCode Integer error code in JSON response, if any
     * @param responseBody responseBody body as string
     * @param responseBodyJson responseBody body as Json Map
     */
    public JsonRpcStatusException(String message, int httpCode, String httpMessage, int jsonRPCCode, String responseBody, JsonRpcResponse responseBodyJson ) {
        super(message);
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
        this.jsonRPCCode = jsonRPCCode;
        this.response = responseBody;
        this.responseJson = responseBodyJson;
    }
}
