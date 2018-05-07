package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.msgilligan.bitcoinj.json.conversion.NumberStringSerializer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JSON-RPC Request POJO
 */
public class JsonRpcRequest {
    public static final String JSON_RPC_VERSION_1 = "1.0";
    public static final String JSON_RPC_VERSION_2 = "2.0";
    private static final String DEFAULT_JSON_RPC_VERSION = JSON_RPC_VERSION_1;
    private static AtomicLong nextRequestId =  new AtomicLong(0);

    private final String  jsonrpc;   // version
    private final String  method;
    private final String  id;
    private final List<Object> params;

    /**
     * For use by Jackson deserialization
     * @param jsonrpc filled from the JSON object
     * @param method filled from the JSON object
     * @param id filled from the JSON object
     * @param params filled from the JSON object
     */
    @JsonCreator
    public JsonRpcRequest(@JsonProperty("jsonrpc")  String jsonrpc,
                          @JsonProperty("method")   String method,
                          @JsonProperty("id")       String id,
                          @JsonProperty("params")   List<Object> params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.id = id;
        this.params = params;
    }

    /**
     * For creating a JSON RPC request for serialization
     * @param method Method of remote procedure to call
     * @param params Parameters to serialize
     */
    public JsonRpcRequest(String method, List<Object> params) {
        this(method, params, DEFAULT_JSON_RPC_VERSION);
    }

    public JsonRpcRequest(String method, List<Object> params, String jsonRpcVersion) {
        this.jsonrpc = jsonRpcVersion;
        this.id =  Long.toString(JsonRpcRequest.nextRequestId.incrementAndGet());
        this.method = method;
        this.params = removeTrailingNulls(params);
    }

    /**
     * Convenience constructor for requests with empty parameter list
     * @param method method name string
     */
    public JsonRpcRequest(String method) {
        this(method, Collections.emptyList());
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    @JsonSerialize(using=NumberStringSerializer.class)
    public String getId() {
        return id;
    }

    public List<Object> getParams() {
        return params;
    }

    /**
     * Remove trailing nulls (all nulls *following* the last non-null object)
     *
     * This allows convenience methods to use `null` parameters to indicate the
     * server-determined default should be used. If `null` were actually passed as
     * JSON, then the server default would be overridden.  `null` can be used before
     * the last non-null element, but those `null`s will be sent to the server.
     */
    private static List<Object> removeTrailingNulls(List<Object> params) {
        LinkedList<Object> cleaned = new LinkedList<>(params);
        while ((cleaned.size() > 0) && (cleaned.getLast() == null)) {
            cleaned.removeLast();
        }
        return cleaned;
    }
}
