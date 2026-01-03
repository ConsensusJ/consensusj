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
        this.params = List.copyOf(params);
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
        this(jsonRpcVersion.jsonrpc(), Long.toString(id), method, params);
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
     * Remove trailing {@code null}s (all {@code null}s </i>following</i> the last non-{@code null}  object)
     * <p>
     * This allows convenience methods to use {@code null} parameters to indicate the
     * server-determined default should be used. If {@code null} were actually sent as
     * JSON, then the server default would be overridden.  {@code null}  can be used before
     * the last non-null element, but those {@code null} s will be sent to the server.
     * @param params A list of parameters possibly containing trailing nulls
     * @return A list of parameters with the trailing nulls removed.
     * @param <T> The type of the elements (for JSON-RPC this is usually {@link Object})
     */
    private static <T> List<T> removeTrailingNulls(List<T> params) {
        LinkedList<T> cleaned = new LinkedList<>(params);
        while (!cleaned.isEmpty() && cleaned.getLast() == null) {
            cleaned.removeLast();
        }
        return cleaned;
    }
}
