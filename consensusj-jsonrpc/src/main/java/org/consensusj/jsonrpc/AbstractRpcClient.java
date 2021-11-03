package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.consensusj.jsonrpc.util.Base64;

/**
 * Abstract Base class for a strongly-typed, Jackson-based JSON-RPC client. Most of the work is done
 * in default methods in {@link JacksonRpcClient} This abstract class implements the constructors, static fields, and
 * getters, but leaves the core {@code sendRequestForResponse} method as {@code abstract} to be implemented by subclasses
 * allowing implementation with alternative HTTP client libraries.
 */
public abstract class AbstractRpcClient implements JacksonRpcClient {
    /**
     * The default JSON-RPC version in JsonRpcRequest is now '2.0', but since most
     * requests are created inside {@code RpcClient} subclasses, we'll continue to default
     * to '1.0' in this base class.
     */
    private static final JsonRpcMessage.Version DEFAULT_JSON_RPC_VERSION = JsonRpcMessage.Version.V1;

    protected final JsonRpcMessage.Version jsonRpcVersion;
    protected final ObjectMapper mapper;
    private final JavaType defaultType;

    /**
     * @deprecated Specify JSON-RPC version and use {@link AbstractRpcClient#AbstractRpcClient(JsonRpcMessage.Version)}
     */
    @Deprecated
    public AbstractRpcClient() {
        this(DEFAULT_JSON_RPC_VERSION);
    }

    public AbstractRpcClient(JsonRpcMessage.Version jsonRpcVersion) {
        this.jsonRpcVersion = jsonRpcVersion;
        mapper = new ObjectMapper();
        // TODO: Provide external API to configure FAIL_ON_UNKNOWN_PROPERTIES
        // TODO: Remove "ignore unknown" annotations on various POJOs that we've defined.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultType = mapper.getTypeFactory().constructType(Object.class);
    }

    @Override
    public JsonRpcMessage.Version getJsonRpcVersion() {
        return jsonRpcVersion;
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public JavaType getDefaultType() {
        return defaultType;
    }

    /**
     * Encode username password as Base64 for basic authentication
     *
     * We're using an internal `Base64` utility class here (copied from Android) in order
     * to have working, consistent behavior on JavaSE and Android. Prior to Android 8.0,
     * Android has its own implementation that differs from the JavaSE version.
     *
     * @param authString An authorization string of the form `username:password`
     * @return A compliant Base64 encoding of `authString`
     */
    protected static String base64Encode(String authString) {
        return Base64.encodeToString(authString.getBytes(),Base64.NO_WRAP).trim();
    }
}
