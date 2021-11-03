package org.consensusj.jsonrpc.javanet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.consensusj.jsonrpc.AbstractRpcClient;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
 * Incubating JSON-RPC client using java.net.http.
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
        super(jsonRpcVersion);
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(2))
                .build();
    }

    public JsonRpcClientJavaNet(URI server, final String rpcUser, final String rpcPassword) {
        this(JsonRpcMessage.Version.V2, server, rpcUser, rpcPassword);
    }

    @Override
    public <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        //TypeReference<Map<Address, OmniwalletAddressBalance>> typeRef = new TypeReference<>() {};
        HttpRequest httpRequest = buildJsonRpcPostRequest(request);
        return (JsonRpcResponse<R>) sendForResponseAsync(httpRequest, responseType).join();

    }

    public String sendRequestForResponseString(JsonRpcRequest request) throws IOException, JsonRpcStatusException {
        HttpRequest httpRequest = buildJsonRpcPostRequest(request);
        return sendForStringAsync(httpRequest).join();

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

    private <R> CompletableFuture<JsonRpcResponse<R>> sendForResponseAsync(HttpRequest request, JavaType responseType) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(responseType));
    }

    private CompletableFuture<String> sendForStringAsync(HttpRequest request) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request);
    }

    private <R> CompletableFuture<R> sendAsync(HttpRequest request, Class<R> clazz) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(clazz));
    }

    private <R> CompletableFuture<R> sendAsync(HttpRequest request, JavaType responseType) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(responseType));
    }

    private <R> CompletableFuture<R> sendAsync(HttpRequest request, TypeReference<R> typeReference) {
        log.debug("Send aysnc: {}", request);
        return sendAsyncCommon(request)
                .thenApply(mappingFunc(typeReference));
    }

    private CompletableFuture<String> sendAsyncCommon(HttpRequest request) {
        log.debug("Send aysnc: {}", request);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete(JsonRpcClientJavaNet::log)
                .thenApply(HttpResponse::body)
                .whenComplete(JsonRpcClientJavaNet::log);
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
                .newBuilder(serverURI.resolve(""))
                .header("Content-Type", "application/json;charset=" +  UTF8)
                .header("Accept-Charset", UTF8)
                .header("Accept", "application/json")
                .header ("Authorization", basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(requestString))
                .build();
    }


    private <R> MappingFunction<R> mappingFunc(Class<R> clazz) {
        return s -> mapper.readValue(s, clazz);
    }

    private <R> MappingFunction<R> mappingFunc(JavaType responseType) {
        return s -> mapper.readValue(s, responseType);
    }

    private <R> MappingFunction<R> mappingFunc(TypeReference<R> typeReference) {
        return s -> mapper.readValue(s, typeReference);
    }

    @FunctionalInterface
    interface MappingFunction<R> extends ThrowingFunction<String, R> {}

    @FunctionalInterface
    interface ThrowingFunction<T,R> extends Function<T, R> {

        /**
         * Gets a result wrapping checked Exceptions with {@link RuntimeException}
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
         * Gets a result.
         *
         * @return a result
         * @throws Exception Any checked Exception
         */
        R applyThrows(T t) throws Exception;
    }
    
    private static void log(HttpResponse<String> httpResponse, Throwable t) {
        if ((httpResponse != null)) {
            log.info("log data string: {}", httpResponse);
        } else {
            log.error("exception: ", t);
        }
    }

    private static void log(String s, Throwable t) {
        if ((s != null)) {
            log.info("log data string: {}", s.substring(0 ,Math.min(100, s.length())));
        } else {
            log.error("exception: ", t);
        }
    }
}
