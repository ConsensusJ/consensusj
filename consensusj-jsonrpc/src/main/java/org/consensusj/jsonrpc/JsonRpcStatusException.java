package org.consensusj.jsonrpc;

/**
 * JSON-RPC returned HTTP status other than 200 (and unfortunately also sometimes when their is response.error)
 * Additional information is usually in JSON-RPC response
 * TODO: See TODO in parent class {@link JsonRpcException}
 * @see org.consensusj.jsonrpc.JsonRpcException
 */
public class JsonRpcStatusException extends JsonRpcException {
    public final String httpMessage;
    public final int httpCode;
    public final int jsonRpcCode;

    /**
     * Http response body as a string. Null if not-available (check deserialized JSON in this case)
     */
    public final String response;

    /**
     * Deserialized response message, if available. Null if not-available.
     * Result is usually null when an error occurs, but the type of the result is
     * unspecified and could be either {@link java.util.Map}, {@link com.fasterxml.jackson.databind.JsonNode},
     * or the result type of the failed request depending upon where and how the exception was
     * created.
     */
    public final JsonRpcResponse<?> responseJson;

    /**
     * Canonical Constructor
     *
     * @param message Error message from Json if available, else http status message
     * @param httpCode HTTP status code, e.g. 404
     * @param httpMessage HTTP status message, e.g. "Not found" (removed from HTTP/2 and HTTP/3)
     * @param jsonRPCCode Integer error code in JSON response, if any
     * @param responseBody responseBody body as string (null if JSON available)
     * @param responseBodyJson responseBody body as Json Map (null if JSON not-available)
     */
    public JsonRpcStatusException(String message, int httpCode, String httpMessage, int jsonRPCCode, String responseBody, JsonRpcResponse<?> responseBodyJson ) {
        super(message);
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
        this.jsonRpcCode = jsonRPCCode;
        this.response = responseBody;
        this.responseJson = responseBodyJson;
    }

    /**
     * Same as canonical, but without the {@code httpCode} parameter. (which is not present in java.net.http, HTTP/2, etc.)
     */
    public JsonRpcStatusException(String message, int httpCode, int jsonRPCCode, String responseBody, JsonRpcResponse<?> responseBodyJson ) {
        this(message, httpCode, "", jsonRPCCode, responseBody, responseBodyJson);
    }

    /**
     * Constructor for when we were able to deserialize a JSON response
     * @param httpCode http status code
     * @param responseBodyJson deserialized JSON
     */
    public JsonRpcStatusException(int httpCode, JsonRpcResponse<?> responseBodyJson) {
        this(responseBodyJson.getError().getMessage(),
                httpCode,
                responseBodyJson.getError() != null ? responseBodyJson.getError().getCode() : 0,
                null,
                responseBodyJson);
    }

    /**
     * Constructor for when we were unable to deserialize a JSON response
     * @param httpCode http status code
     * @param responseBody response body as a string
     */
    public JsonRpcStatusException(int httpCode, String responseBody) {
        this(responseBody, httpCode, 0, responseBody, null);
    }
}
