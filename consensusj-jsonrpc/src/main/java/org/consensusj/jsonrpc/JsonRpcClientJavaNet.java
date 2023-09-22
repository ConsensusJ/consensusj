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

/**
 * Incubating JSON-RPC client using {@link java.net.http.HttpClient}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        return sendCommon(request)
                .thenApply(mappingFunc(responseType));
    }

    // For testing only
    CompletableFuture<String> sendRequestForResponseString(JsonRpcRequest request) {
        return sendCommon(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getServerURI() {
        return serverURI;
    }

    private String encodeJsonRpcRequest(JsonRpcRequest request) throws JsonProcessingException {
        return mapper.writeValueAsString(request);
    }

    /**
     * @param request A JSON-RPC request
     * @return A future for a JSON-RPC response in String format
     */
    private CompletableFuture<String> sendCommon(JsonRpcRequest request) {
        log.debug("Send: {}", request);
        try {
            HttpRequest httpRequest = buildJsonRpcPostRequest(request);
            return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .whenComplete(this::log)
                    .thenComposeAsync(this::handleStatusError)
                    .thenApply(HttpResponse::body)
                    .whenComplete(this::log);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<HttpResponse<String>> handleStatusError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            String errorResponse = response.body();
            log.warn("Bad status code: {}: {}", response.statusCode(), errorResponse);
            // TODO: If the error Response is JSON (which it is for method not found, at least) it shouldn't be the "message" in the exception
            return CompletableFuture.failedFuture(new JsonRpcStatusException(errorResponse, response.statusCode(), errorResponse, -1, errorResponse, null));

        } else {
            return CompletableFuture.completedFuture(response);
        }
    }

    private HttpRequest buildJsonRpcPostRequest(JsonRpcRequest request) throws JsonProcessingException {
        String requestString = encodeJsonRpcRequest(request);
        return buildJsonRpcPostRequest(requestString);
    }

    private HttpRequest buildJsonRpcPostRequest(String requestString) {
        log.info("request is: {}", requestString);

        String auth = username + ":" + password;
        String basicAuth = "Basic " + base64Encode(auth);

        return HttpRequest
                .newBuilder(serverURI)
                .header("Content-Type", "application/json;charset=" +  UTF8)
                .header("Accept-Charset", UTF8)
                .header("Accept", "application/json")
                .header("Authorization", basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(requestString))
                .build();
    }

    private <R> MappingFunction<R> mappingFunc(JavaType responseType) {
        return s -> mapper.readValue(s, responseType);
    }

    /**
     * Map a response string to a Java object. Wraps checked {@link JsonProcessingException}
     * in unchecked {@link CompletionException}.
     * @param <R> result type
     */
    @FunctionalInterface
    protected interface MappingFunction<R> extends Function<String, R> {

        /**
         * Gets a result. Wraps checked {@link JsonProcessingException} in {@link CompletionException}
         * @param s input
         * @return a result
         */
        @Override
        default R apply(String s) {
            try {
                return applyThrows(s);
            } catch (JsonProcessingException e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Gets a result and may throw a checked exception.
         *
         * @param s input
         * @return a result
         * @throws JsonProcessingException Checked Exception
         */
        R applyThrows(String s) throws JsonProcessingException;
    }
    
    private void log(HttpResponse<String> httpResponse, Throwable t) {
        if ((httpResponse != null)) {
            log.info("log data string: {}", httpResponse);
        } else {
            log.warn("exception: ", t);
        }
    }

    private void log(String s, Throwable t) {
        if ((s != null)) {
            log.info("log data string: {}", s.substring(0 ,Math.min(100, s.length())));
        } else {
            log.warn("exception: ", t);
        }
    }
}
