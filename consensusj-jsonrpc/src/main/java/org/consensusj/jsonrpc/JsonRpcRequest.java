package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.consensusj.jsonrpc.internal.NumberStringSerializer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.consensusj.jsonrpc.JsonRpcMessage.Version.*;

/**
 * JSON-RPC Request POJO
 */
public class JsonRpcRequest {
    /**
     * @deprecated Use {@link JsonRpcMessage.Version#V1} enum instead
     */
    @Deprecated
    public static final String JSON_RPC_VERSION_1 = V1.jsonrpc();
    /**
     * @deprecated Use {@link JsonRpcMessage.Version#V2} enum instead
     */
    @Deprecated
    public static final String JSON_RPC_VERSION_2 = V2.jsonrpc();
    private static final JsonRpcMessage.Version DEFAULT_JSON_RPC_VERSION = V2;
    private static final AtomicLong nextRequestId =  new AtomicLong(0);

    private final String  method;
    private final List<Object> params;
    private final String  jsonrpc;   // version
    private final String  id;

    /**
     * Constructor for use by Jackson deserialization. Deserialization will typically
     * be used on the server-side. To create a request on the client side, it
     * is generally recommended to use the methods in {@link JsonRpcClient}.
     *
     * @param jsonrpc filled from the JSON object
     * @param id filled from the JSON object
     * @param method filled from the JSON object
     * @param params filled from the JSON object
     * @see JsonRpcClient#buildJsonRequest(String, List)
     * @see JsonRpcClient#buildJsonRequest(String, Object...)
     */
    @JsonCreator
    public JsonRpcRequest(@JsonProperty("jsonrpc")  String jsonrpc,
                          @JsonProperty("id")       String id,
                          @JsonProperty("method")   String method,
                          @JsonProperty("params")   List<Object> params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.id = id;
        this.params = params;
    }

    /**
     * This constructor is slightly more strict than the deserialization constructor and should be used
     * where possible. It uses the {@link JsonRpcMessage.Version} {@code enum} and requires that
     * the {@code id} be a {@code long}.
     *
     * @param jsonRpcVersion JSON-RPC version (enum)
     * @param id message id
     * @param method RPC {@code method}
     * @param params RPC method positional parameters
     */
    public JsonRpcRequest(JsonRpcMessage.Version jsonRpcVersion,
                          long id,
                          String method,
                          List<Object> params) {
        this.jsonrpc    = jsonRpcVersion.jsonrpc();
        this.method     = method;
        this.id         = Long.toString(id);
        this.params     = params;
    }

    /**
     * Create a JSON RPC request (for serialization.)
     * Can be used to override default JSON RPC version. To create a request on the client side, it
     * is generally recommended to use the methods in the {@link JsonRpcClient} you are using.
     *
     * @param jsonRpcVersion JSON-RPC version (enum)
     * @param method Method of remote procedure to call
     * @param params Parameters to serialize
     */
    public JsonRpcRequest(JsonRpcMessage.Version jsonRpcVersion, String method, List<Object> params) {
        this(jsonRpcVersion, JsonRpcRequest.nextRequestId.incrementAndGet(), method, removeTrailingNulls(params));
    }

    /**
     * Create a JSON RPC request (for serialization.) To create a request on the client side, it
     * is generally recommended to use the methods in {@link JsonRpcClient}.
     *
     * @param method Method of remote procedure to call
     * @param params Parameters to serialize
     * @see JsonRpcClient#buildJsonRequest(String, List)
     * @see JsonRpcClient#buildJsonRequest(String, Object...)
     */
    public JsonRpcRequest(String method, List<Object> params) {
        this(DEFAULT_JSON_RPC_VERSION, method, params);
    }

    /**
     * Create a JSON RPC request (for serialization.)
     * Can be used to override default JSON RPC version. To create a request on the client side, it
     * is generally recommended to use the methods in {@link JsonRpcClient}.
     * 
     * @param method Method of remote procedure to call
     * @param params Parameters to serialize
     * @param jsonRpcVersionString JSON-RPC version string
     * @see JsonRpcClient#buildJsonRequest(String, List)
     * @see JsonRpcClient#buildJsonRequest(String, Object...)
     * @deprecated Use {@link JsonRpcRequest#JsonRpcRequest(JsonRpcMessage.Version, String, List)}
     */
    @Deprecated
    public JsonRpcRequest(String method, List<Object> params, String jsonRpcVersionString) {
        this(jsonRpcVersionString, Long.toString(JsonRpcRequest.nextRequestId.incrementAndGet()), method, removeTrailingNulls(params));
    }

    /**
     * Convenience constructor for requests with empty parameter list
     * @param method method name string
     */
    public JsonRpcRequest(String method) {
        this(method, List.of());
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
