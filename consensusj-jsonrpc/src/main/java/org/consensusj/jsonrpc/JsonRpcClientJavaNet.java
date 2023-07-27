package org.consensusj.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

// TODO: Add ability to pass an sslSocketFactory (or equivalent) in the constructor.
/**
 * Incubating JSON-RPC client using {@link java.net.http.HttpClient}
 * Synchronous API only for now (internal implementation is async), will add async API later.
 */
public class JsonRpcClientJavaNet extends AbstractRpcClient {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcClientJavaNet.class);
    private final URI serverURI;
    private final String username;
    private final String password;
    private final HttpClient client;
    private static final String UTF8 = StandardCharsets.UTF_8.name();


    public JsonRpcClientJavaNet(JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        this(getDefaultSSLContext(), jsonRpcVersion, server, rpcUser, rpcPassword);
    }

    public JsonRpcClientJavaNet(SSLContext sslContext, JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        super(jsonRpcVersion);
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(2))
                .sslContext(sslContext)
                .build();
    }

    public JsonRpcClientJavaNet(URI server, final String rpcUser, final String rpcPassword) {
        this(JsonRpcMessage.Version.V2, server, rpcUser, rpcPassword);
    }

    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        log.debug("Send aysnc: {}", request);
        try {
            HttpRequest httpRequest = buildJsonRpcPostRequest(request);
            return sendAsyncCommon(httpRequest)
                    .thenApply(mappingFunc(responseType));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // For testing only
    CompletableFuture<String> sendRequestForResponseStringAsync(JsonRpcRequest request) {
        log.debug("Send aysnc: {}", request);
        try {
            HttpRequest httpRequest = buildJsonRpcPostRequest(request);
            return sendAsyncCommon(httpRequest);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get the URI of the server this client connects to
     * @return Server URI
     */
    @Override
    public URI getServerURI() {
        return serverURI;
    }

    private String encodeJsonRpcRequest(JsonRpcRequest request) throws JsonProcessingException {
        return mapper.writeValueAsString(request);
    }

    private CompletableFuture<String> sendAsyncCommon(HttpRequest request) {
        log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete(this::log)
                .thenComposeAsync(this::handleStatusError)
                .thenApply(HttpResponse::body)
                .whenComplete(this::log);
    }

    private CompletableFuture<HttpResponse<String>> handleStatusError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            String errorResponse = response.body();
            log.error("Bad status code: {}: {}", response.statusCode(), errorResponse);
            return CompletableFuture.failedFuture(new JsonRpcStatusException(errorResponse, response.statusCode(), errorResponse, -1, errorResponse, null));

        } else {
            return CompletableFuture.completedFuture(response);
        }
    }

    private HttpRequest buildJsonRpcPostRequest(JsonRpcRequest request) throws JsonProcessingException {
        String requestString = encodeJsonRpcRequest(request);
        return buildJsonRpcPostRequest(requestString);
    }

    private HttpRequest buildJsonRpcPostRequest(String requestString) throws JsonProcessingException {
        log.info("request is: {}", requestString);

        String auth = username + ":" + password;
        String basicAuth = "Basic " + base64Encode(auth);

        return HttpRequest
                .newBuilder(serverURI)
                .header("Content-Type", "application/json;charset=" +  UTF8)
                .header("Accept-Charset", UTF8)
                .header("Accept", "application/json")
                .header ("Authorization", basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(requestString))
                .build();
    }

    private <R> MappingFunction<R> mappingFunc(JavaType responseType) {
        return s -> mapper.readValue(s, responseType);
    }

    /**
     * Map a response string to Java object
     * @param <R> Desired type of Java object
     */
    @FunctionalInterface
    protected interface MappingFunction<R> extends ThrowingFunction<String, R> {}

    /**
     * Utility interface for declaring functions that throw checked exceptions and wrapping
     * them in a {@link Function} that will throw {@link RuntimeException} if the underlying
     * {@link #applyThrows(Object)} method throws a checked {@link Exception}.
     * @param <T> input type
     * @param <R> result type
     */
    @FunctionalInterface
    protected interface ThrowingFunction<T,R> extends Function<T, R> {

        /**
         * Gets a result. Wraps checked Exceptions with {@link RuntimeException}
         * @param t input
         * @return a result
         */
        @Override
        default R apply(T t) {
            try {
                return applyThrows(t);
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Gets a result and may throw a checked exception.
         *
         * @param t input
         * @return a result
         * @throws Exception Any checked Exception
         */
        R applyThrows(T t) throws Exception;
    }
    
    private void log(HttpResponse<String> httpResponse, Throwable t) {
        if ((httpResponse != null)) {
            log.info("log data string: {}", httpResponse);
        } else {
            log.error("exception: ", t);
        }
    }

    private void log(String s, Throwable t) {
        if ((s != null)) {
            log.info("log data string: {}", s.substring(0 ,Math.min(100, s.length())));
        } else {
            log.error("exception: ", t);
        }
    }
}
