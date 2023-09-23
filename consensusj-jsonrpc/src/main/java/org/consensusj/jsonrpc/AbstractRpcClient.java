package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

// TODO: Rather than implementing transport (HttpUrlConnection vs. java.net.http) with subclasses use composition
// In other words, the constructor should take a transport implementation object.
// We're overusing inheritance in this hierarchy. We are breaking Effective Java, Item 18: Favor composition over inheritance.
// We're using inheritance to configure:
// A. The set of JSON-RPC methods that are supported, e.g. `getblockcount()` (which creates potential conflicts with `send()` etc)
// B. The JSON mapping implementation. Which for now and the foreseeable future is Jackson only.
// C. The transport implementation.
//
// As we want to support both HttpUrlConnection vs. java.net.http while transitioning to java.net.http, we don't want to force
// subclasses like `BitcoinClient` to choose one or the other. So making (C) transport implementation a separate, composable object
// is the FIRST STEP. Later we can look at separating (A) and (B). My first thoughts on how to do this is:
// (1) Create a JsonRpcClientTransport interface.
// (2) Rename `AbstractRpcClient` to `DefaultJsonRpcClient`, make it concrete and have it take a JsonRpcClientTransport instance as
//     a constructor parameter.
// (3) Create two implementations of `JsonRpcClientTransport` based upon `JsonRpcClientHttpUrlConnection` and `JsonRpcClientJavaNet`.
// (4) Optional. Maybe look at extracting a class with some common "mapper" functions between the two transport implementations
//
// To separate (C) the easiest way is probably via a constructor parameter.
// The proper separation for (A) is probably a complete separation. There should be no required inheritance to implement
// a client with a set of methods. Internally the client would have a transport and a mapper and those could optionally be made available
// via some accessor methods if the client application deems necessary.
// At the heart of (B) (at least as currently implemented) is the mapping from a Java (or Groovy) method name to (i) a JSON-RPC method
// name, (ii) optional parameter type-conversion for JSON serialization in the request, and (iii) type mapping for the deserialization
// of the `result` field in the JsonRpcResponse<RSLT>. It might be helpful to think of this as two functional mappings:
// (1) Map Java method name and parameters to a JSON-RPC request (either map to set of Java Objects _or_ all the way to JSON)
// (2) Map from received JSON-RPC response to JsonRpcResponse<RSLT> -- this response mapper function is configured as part of making the request.
//
// The SECOND STEP is to abstract the specifics of Jackson from the (two) transport implementations. Basically methods/functions to
// map from request to string/stream and to map from string/stream to response.  The java.net.http implementation has already defined
// some functional interfaces for this, so coming up with an interface that both the java.net.http implementation and the HttpUrlConnection
// implementation can use will lead to this "SECOND STEP"
//
// Update: Now that JsonRpcClient is a generic with <T extends Type>, we have loosened the Jackson coupling somewhat. The sendRequestForResponse
// and sendRequestForResponseAsync methods from JsonRpcClient have been moved to the JsonRpcTransport class which the JavaNet and HttpUrlConnection
// flavors implement. The AbstractRpcClient constructor should be passed an instance of either transport class and forward methods calls for
// sendRequestForResponseAsync (and sendRequestForResponse ?) to the transport.
/**
 * Abstract Base class for a strongly-typed, Jackson-based JSON-RPC client. This abstract class implements the constructors, static fields, and
 * getters, but leaves the core {@code sendRequestForResponse} method as {@code abstract} to be implemented by subclasses
 * allowing implementation with alternative HTTP client libraries.
 */
public abstract class AbstractRpcClient implements JsonRpcClient<JavaType> {
    /**
     * The default JSON-RPC version in JsonRpcRequest is now '2.0', but since most
     * requests are created inside {@code RpcClient} subclasses, we'll continue to default
     * to '1.0' in this base class.
     */
    private static final JsonRpcMessage.Version DEFAULT_JSON_RPC_VERSION = JsonRpcMessage.Version.V1;

    protected final JsonRpcMessage.Version jsonRpcVersion;
    protected final ObjectMapper mapper;
    private final JavaType defaultType;

    public AbstractRpcClient(JsonRpcMessage.Version jsonRpcVersion) {
        this.jsonRpcVersion = jsonRpcVersion;
        mapper = new ObjectMapper();
        // TODO: Provide external API to configure FAIL_ON_UNKNOWN_PROPERTIES
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultType = mapper.getTypeFactory().constructType(Object.class);
    }

    @Override
    public JsonRpcMessage.Version getJsonRpcVersion() {
        return jsonRpcVersion;
    }

    /**
     * Convenience method for requesting an asynchronous response with a {@link JsonNode} for the result.
     * @param request The request to send
     * @return A future JSON RPC Response with `result` of type {@code JsonNode}
     */
    public CompletableFuture<JsonRpcResponse<JsonNode>> sendRequestForResponseAsync(JsonRpcRequest request) {
        return sendRequestForResponseAsync(request, responseTypeFor(JsonNode.class));
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public JavaType defaultType() {
        return defaultType;
    }

    @Override
    public JavaType responseTypeFor(JavaType resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }

    @Override
    public JavaType responseTypeFor(Class<?> resultType) {
        return getMapper().getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }

    @Override
    public JavaType typeForClass(Class<?> clazz) {
        return getMapper().constructType(clazz);
    }

}
