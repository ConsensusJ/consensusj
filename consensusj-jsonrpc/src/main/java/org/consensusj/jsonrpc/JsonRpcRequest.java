/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.consensusj.jsonrpc.internal.NumberStringSerializer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.consensusj.jsonrpc.JsonRpcMessage.Version.*;

/**
 * JSON-RPC Request.
 * Contains the four standard members of a JSON-RPC request:
 * <ul>
 *     <li><b>jsonrpc</b>: the version of the JSON-RPC protocol. The default is {@code "2.0"}.</li>
 *     <li><b>id</b>: an identifier sent in request that is returned in {@link JsonRpcResponse}. By default, we use
 *     atomically-incremented {@code long} values, but {@code String} values are also supported as they are allowed
 *     by the specification and may be sent by clients.</li>
 *     <li><b>method:</b> the name of the method to be invoked.</li>
 *     <li><b>params:</b> the parameters for the rpc call. Currently only {@code by-position} parameters are supported, represented
 *     as a Java {@link List} and serialized as a JSON {@code Array}.</li>
 * </ul>
 * Objects in the parameter list {@link #getParams()} are {@link Nullable}. {@code null}s are used to send a JSON {@code null}
 * to the server for that position in the list. When using the {@link #JsonRpcRequest(JsonRpcMessage.Version, String, List)} constructor,
 * trailing {@code null}s (i.e. {@code null}s that occur after the last non{@code null} parameter) are removed so that server defaults for
 * those parameters are used. If you are using {@code null}s in your requests be aware of which constructors you use (directly or indirectly.)
 * Making this behavior more consistent would be a breaking change, so it won't be considered until a future release.
 */
public class JsonRpcRequest {
    private static final JsonRpcMessage.Version DEFAULT_JSON_RPC_VERSION = V2;
    private static final AtomicLong nextRequestId =  new AtomicLong(0);

    private final String  method;
    private final List<@Nullable Object> params;
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
    public JsonRpcRequest(@JsonProperty("jsonrpc")  @Nullable String jsonrpc,
                          @JsonProperty("id")       String id,
                          @JsonProperty("method")   String method,
                          @JsonProperty("params")   @Nullable List<@Nullable Object> params) {
        this.jsonrpc = jsonrpc != null ? jsonrpc : V1.jsonrpc();
        this.method = method;
        this.id = id;
        this.params = params != null ? Collections.unmodifiableList(new ArrayList<>(params)) : List.of();
    }

    /**
     * This constructor is more strongly-typed than the deserialization constructor and should be preferred to that
     * constructor where possible.
     * It uses the {@link JsonRpcMessage.Version} {@code enum} and uses a {@code long} to specify
     * the {@code id} value (which is stored internally as a {@link String}.
     * a {@code long}.
     *
     * @param jsonRpcVersion JSON-RPC version (enum)
     * @param id message id
     * @param method RPC {@code method}
     * @param params RPC method positional parameters
     */
    public JsonRpcRequest(JsonRpcMessage.Version jsonRpcVersion,
                          long id,
                          String method,
                          List<@Nullable Object> params) {
        this(jsonRpcVersion.jsonrpc(), Long.toString(id), method, params);
    }

    /**
     * Create a JSON RPC request (for serialization.)
     * Can be used to override default JSON RPC version. To create a request on the client side, it
     * is generally recommended to use the methods in the {@link JsonRpcClient} you are using.
     * <p>
     * In this constructor, trailing {@code null}s are removed from {@code params} so that those parameters
     * are not sent to the server and do not override server defaults.
     * @param jsonRpcVersion JSON-RPC version (enum)
     * @param method Method of remote procedure to call
     * @param params Parameters to serialize
     */
    public JsonRpcRequest(JsonRpcMessage.Version jsonRpcVersion, String method, List<@Nullable Object> params) {
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
    public JsonRpcRequest(String method, List<@Nullable Object> params) {
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

    /**
     * Get the JSON-RPC {@code id} member.
     * <p>
     * According to the JSON-RPC Specification {@code id} can be a {@code String}, {@code Number},
     * or {@code NULL}. We generally represent {@code id} as a Java {@link String}. However, when serialization with
     * Jackson, we use {@link NumberStringSerializer} to produce a JavaScript {@code Number} (integer) when possible.
     * This was necessary for compatibility with certain servers.
     * {@code JsonRpcRequest#getId()} with
     * @return The id as a string
     */
    @JsonSerialize(using=NumberStringSerializer.class)
    public String getId() {
        return id;
    }

    public List<@Nullable Object> getParams() {
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
    static <T> List<@Nullable T> removeTrailingNulls(List<@Nullable T> params) {
        LinkedList<@Nullable T> cleaned = new LinkedList<>(params);
        while (!cleaned.isEmpty() && cleaned.getLast() == null) {
            cleaned.removeLast();
        }
        return cleaned;
    }
}
