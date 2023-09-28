package org.consensusj.jsonrpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.lang.reflect.Type;
import java.time.Duration;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: the constructor should take a JsonRpcTransport implementation object.
// We now have DefaultRpcClient which is currently hard-coded to use the JavaNet transport (1-line change + recompile to switch)

// We're overusing inheritance in this hierarchy. We are breaking Effective Java, Item 18: Favor composition over inheritance.
// We're using inheritance to configure:
// A. The set of JSON-RPC methods that are supported, e.g. `getblockcount()` (which creates potential conflicts with `send()` etc)
// B. The JSON mapping implementation. Which for now and the foreseeable future is Jackson only.
//
// TODO: Maybe look at extracting a class with some common "mapper" functions between the two transport implementations
//
// The proper separation for (A) is probably a complete separation. There should be no required inheritance to implement
// a client with a set of methods. Internally the client would have a transport and a mapper and those could optionally be made available
// via some accessor methods if the client application deems necessary.
// At the heart of (B) (at least as currently implemented) is the mapping from a Java (or Groovy) method name to (i) a JSON-RPC method
// name, (ii) optional parameter type-conversion for JSON serialization in the request, and (iii) type mapping for the deserialization
// of the `result` field in the JsonRpcResponse<RSLT>. It might be helpful to think of this as two functional mappings:
// (1) Map Java method name and parameters to a JSON-RPC request (either map to set of Java Objects _or_ all the way to JSON)
// (2) Map from received JSON-RPC response to JsonRpcResponse<RSLT> -- this response mapper function is configured as part of making the request.
//
// To abstract the specifics of Jackson from the (two) transport implementations. Basically methods/functions to
// map from request to string/stream and to map from string/stream to response.  The java.net.http implementation has already defined
// some functional interfaces for this, so coming up with an interface that both the java.net.http implementation and the HttpUrlConnection
// implementation can use will lead to this "SECOND STEP"
//
// Now that JsonRpcClient is a generic with <T extends Type>, we have loosened the Jackson coupling somewhat.
//
/**
 * A strongly-typed, Jackson-based JSON-RPC client. {@link JsonRpcClient} provides many convenience send `default` methods in a
 * JSON-library-independent way. DefaultRpcClient provides the needed implementation support for Jackson. This class implements
 * the constructors, static fields, and getters, but delegates the core
 * {@link JsonRpcTransport#sendRequestForResponseAsync(JsonRpcRequest, Type)} method to a {@link JsonRpcTransport} implementation component.
 */
public class DefaultRpcClient implements JsonRpcClient<JavaType> {
    private static final Logger log = LoggerFactory.getLogger(DefaultRpcClient.class);
    /**
     * The default JSON-RPC version in JsonRpcRequest is now '2.0', but since most
     * requests are created inside {@code RpcClient} subclasses, we'll continue to default
     * to '1.0' in this base class.
     */
    private static final JsonRpcMessage.Version DEFAULT_JSON_RPC_VERSION = JsonRpcMessage.Version.V1;

    protected final JsonRpcMessage.Version jsonRpcVersion;
    protected final ObjectMapper mapper;
    private final JavaType defaultType;
    private final JsonRpcTransport<JavaType> transport;

    public DefaultRpcClient(JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        this(JsonRpcTransport.getDefaultSSLContext(), jsonRpcVersion, server, rpcUser, rpcPassword);
    }
    public DefaultRpcClient(SSLContext sslContext, JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        this.jsonRpcVersion = jsonRpcVersion;
        mapper = new ObjectMapper();
        // TODO: Provide external API to configure FAIL_ON_UNKNOWN_PROPERTIES
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultType = mapper.getTypeFactory().constructType(Object.class);
        // TODO: Right now you can manually switch between JsonRpcClientJavaNet and JsonRpcClientHttpUrlConnection by
        //  commenting out one line or the other and recompiling. There needs to be a new constructor which will
        //  allow the default (JsonRpcClientJavaNet) to be overridden.
        transport = new JsonRpcClientJavaNet(mapper, sslContext, server, rpcUser, rpcPassword);
        //transport = new JsonRpcClientHttpUrlConnection(mapper, sslContext, server, rpcUser, rpcPassword);
    }
    
    @Override
    public JsonRpcMessage.Version getJsonRpcVersion() {
        return jsonRpcVersion;
    }

    @Override
    public URI getServerURI() {
        return transport.getServerURI();
    }

    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        return transport.sendRequestForResponseAsync(request, responseType);
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

    @Override
    public JavaType collectionTypeForClasses(Class<? extends Collection> collection, Class<?> clazz) {
        return getMapper().getTypeFactory()
                .constructCollectionType(collection, clazz);
    }

    @Override
    public JavaType collectionTypeForClasses(Class<? extends Collection> collection, JavaType type) {
        return getMapper().getTypeFactory()
                .constructCollectionType(collection, type);
    }

    public <T> CompletableFuture<JsonRpcResponse<T>> pollOnce(JsonRpcRequest request, JavaType resultType, TransientErrorMapper<T> errorMapper) {
        CompletableFuture<JsonRpcResponse<T>> f = sendRequestForResponseAsync(request, responseTypeFor(resultType));
        // In Java 12+ this can be replaced with exceptionallyCompose()
        return f.handle((r, t) -> errorMapper.map(request, r, t))
                .thenCompose(Function.identity());
    }

    /**
     * A wait-for-server routine that is agnostic about which RPC methods the server supports. In addition to two {@link Duration}
     * parameters, there are 2 lambda parameters to enable this method to work with any JSON-RPC server. This version will tell
     * the JSON-RPC Mapper to return an {@link JsonRpcResponse} with a result of type {@link Object}. If you need more precise
     * control over the type, use {@link #waitForServer(Duration, Duration, Supplier, JavaType, TransientErrorMapper)}.
     * @param timeout how long to wait
     * @param retry delay between retries
     * @param requestSupplier supplier of requests (needs to increment request ID at the very least)
     * @param errorMapper function that maps non-fatal errors (i.e. cases to keep polling)
     * @return A future that returns a successful
     * @param <T> The desired result type to be returned when the server is running
     */
    public <T> CompletableFuture<T> waitForServer(Duration timeout, Duration retry, Supplier<JsonRpcRequest> requestSupplier, TransientErrorMapper<T> errorMapper) {
        return waitForServer(timeout,retry, requestSupplier, typeForClass(Object.class), errorMapper);
    }

    // TODO: What happens if you cancel this future?
    /**
    * A wait-for-server routine that is agnostic about which RPC methods the server supports. In addition to two {@link Duration}
    * parameters, there are 3 parameters (2 functions and a generic type specifier) to enable this method to work with any JSON-RPC server.
    * @param timeout how long to wait
    * @param retry delay between retries
    * @param requestSupplier supplier of requests (needs to increment request ID at the very least)
    * @param resultType the result type for the response
    * @param errorMapper function that maps non-fatal errors (i.e. cases to keep polling)
    * @return A future that returns a successful
    * @param <T> The desired result type to be returned when the server is running
    */
    public <T> CompletableFuture<T> waitForServer(Duration timeout, Duration retry, Supplier<JsonRpcRequest> requestSupplier, JavaType resultType, TransientErrorMapper<T> errorMapper) {
        CompletableFuture<T> future = new CompletableFuture<>();
        getDefaultAsyncExecutor().execute(() -> {
            log.debug("Waiting for server RPC ready...");
            String status;          // Status message for logging
            String statusLast = null;
            long seconds = 0;
            while (seconds < timeout.toSeconds()) {
                JsonRpcResponse<T> r;
                try {
                    // All non-fatal exceptions will be mapped to a JsonRpcError with code -20000
                    r = this.pollOnce(requestSupplier.get(), resultType, errorMapper).get();
                } catch (InterruptedException | ExecutionException e) {
                    // If a fatal error occurred, fail our future and abort this thread
                    log.error("Fatal exception: ", e);
                    future.completeExceptionally(e);
                    return;
                }
                if (r.getResult() != null) {
                    // We received a response with a result, server is ready and has returned a usable result
                    log.debug("RPC Ready.");
                    future.complete(r.getResult());
                    return;
                }
                // We received a response with a non-fatal error, log it and wait to retry.
                status = statusFromErrorResponse(r);
                // Log status messages only once, if new or updated
                if (!status.equals(statusLast)) {
                    log.info("Waiting for server: RPC Status: " + status);
                    statusLast = status;
                }
                try {
                    // Damnit, IntelliJ we're not busy-waiting we're polling!
                    Thread.sleep(retry.toMillis());
                    seconds += retry.toSeconds();
                } catch (InterruptedException e) {
                    log.error(e.toString());
                    Thread.currentThread().interrupt();
                    future.completeExceptionally(e);
                    return;
                }
            }
            String timeoutMessage = String.format("waitForServer() timed out after %d seconds", timeout.toSeconds());
            log.error(timeoutMessage);
            future.completeExceptionally(new TimeoutException(timeoutMessage));
        });
        return future;
    }

    /**
     * Functional interface for ignoring what are considered "transient" errors. The definition of what is transient
     * may vary depending upon the application. Different implementations of this function can be created for
     * different applications.
     * <p>
     * The {@code JsonRpcResponse} returned may be a "synthetic" response, that is generated by the client,
     * not by the server. The synthetic response will look like this:
     *     <ul>
     *         <li>error.code: -20000</li>
     *         <li>error.message: "Server temporarily unavailable"</li>
     *         <li>error.data: Detailed string message, e.g. "Connection refused"</li>
     *     </ul>
     * @param <T> The expected result type
     */
    @FunctionalInterface
    public interface TransientErrorMapper<T> {
        /**
         * @param request The request we're handling completions for
         * @param response response if one was successfully returned (or null)
         * @param throwable exception if the call failed (or null)
         * @return A completed or failed future than can replace the input (response, throwable) pair
         */
        CompletableFuture<JsonRpcResponse<T>> map(JsonRpcRequest request, JsonRpcResponse<T> response, Throwable throwable);
    }

    /**
     * Transient error mapper that is a no-op, i.e. it passes all errors through unchanged.
     */
    protected  <T> CompletableFuture<JsonRpcResponse<T>> identityTransientErrorMapper(JsonRpcRequest request, JsonRpcResponse<T> response, Throwable t) {
        return response != null
                ? CompletableFuture.completedFuture(response)
                : CompletableFuture.failedFuture(t);
    }

    protected <T> JsonRpcResponse<T> temporarilyUnavailableResponse(JsonRpcRequest request,  Throwable t) {
        return new JsonRpcResponse<T>(request, new JsonRpcError(-2000, "Server temporarily unavailable", t.getMessage()));
    }

    /**
     * @param response A response where {@code getResult() == null}
     * @return An error status string suitable for log messages
     */
    protected String statusFromErrorResponse(JsonRpcResponse<?> response) {
        Objects.requireNonNull(response);
        if (response.getResult() != null) {
            throw new IllegalStateException("This should only be called for responses with null result");
        }
        if (response.getError() == null) {
            return "Invalid response both result and error were null";
        } else if (response.getError().getData() != null) {
            // Has option data, possibly the -2000 special case
            return response.getError().getData().toString();
        } else {
            return response.getError().getMessage();
        }
    }
}
