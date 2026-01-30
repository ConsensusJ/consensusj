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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Incubating JSON-RPC client using {@link java.net.http.HttpClient}
 */
public class JsonRpcClientJavaNet implements JsonRpcTransport<JavaType> {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcClientJavaNet.class);

    private final ObjectMapper mapper;
    private final URI serverURI;
    private final String username;
    private final String password;
    private final HttpClient client;
    private static final String UTF8 = StandardCharsets.UTF_8.name();


    public JsonRpcClientJavaNet(ObjectMapper mapper, URI server, final String rpcUser, final String rpcPassword) {
        this(mapper, JsonRpcTransport.getDefaultSSLContext(), server, rpcUser, rpcPassword);
    }

    public JsonRpcClientJavaNet(ObjectMapper mapper, SSLContext sslContext, URI server, final String rpcUser, final String rpcPassword) {
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.mapper = mapper;
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(2))
                .sslContext(sslContext)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        return sendCommon(request)
                .thenApply(mappingFuncFor(responseType));
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
                    .thenCompose(this::handleStatusError)
                    .thenApply(HttpResponse::body)
                    .whenComplete(this::log);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Process the HTTP status code. Treats values other than 200 as an error.
     * @param response A received HTTP response.
     * @return Completed or failed future as appropriate.
     */
    private CompletableFuture<HttpResponse<String>> handleStatusError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            String body = response.body();
            log.warn("Bad status code: {}: {}", response.statusCode(), body);
            log.debug("Headers: {}", response.headers());
            // Return a failed future containing a JsonRpcStatusException. Create the exception
            // from a JsonRpcResponse if one can be built, otherwise just use the body string.
            return CompletableFuture.failedFuture(response.headers()
                    .firstValue("Content-Type")
                    .map(s -> s.contains("application/json"))
                    .flatMap(b -> readErrorResponse(body))
                    .map(r -> new JsonRpcStatusException(response.statusCode(), r))
                    .orElse(new JsonRpcStatusException(response.statusCode(), body))
            );
        } else {
            return CompletableFuture.completedFuture(response);
        }
    }

    // Try to read a JsonRpcResponse from a string (error case)
    private Optional<JsonRpcResponse<Object>> readErrorResponse(String body) {
        JsonRpcResponse<Object> response;
        try {
            response = mapper.readValue(body, responseTypeFor(Object.class));
        } catch (JsonProcessingException e) {
            response = null;
        }
        return Optional.ofNullable(response);
    }

    private HttpRequest buildJsonRpcPostRequest(JsonRpcRequest request) throws JsonProcessingException {
        String requestString = encodeJsonRpcRequest(request);
        return buildJsonRpcPostRequest(requestString);
    }

    private HttpRequest buildJsonRpcPostRequest(String requestString) {
        log.info("request is: {}", requestString);

        String auth = username + ":" + password;
        String basicAuth = "Basic " + JsonRpcTransport.base64Encode(auth);

        return HttpRequest
                .newBuilder(serverURI)
                .header("Content-Type", "application/json;charset=" +  UTF8)
                .header("Accept-Charset", UTF8)
                .header("Accept", "application/json")
                .header("Authorization", basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(requestString))
                .build();
    }

    // return a MappingFunction for a given type
    private <R, T extends Type> MappingFunction<R> mappingFuncFor(T responseType) {
        return s -> mapper.readValue(s, (JavaType) responseType);
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
         * @throws CompletionException (unchecked) if a JsonProcessingException exception occurs
         */
        @Override
        default R apply(String s) throws CompletionException {
            try {
                return applyThrows(s);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }

        /**
         * Gets a result and may throw a checked exception.
         * @param s input
         * @return a result
         * @throws JsonProcessingException Checked Exception
         */
        R applyThrows(String s) throws Exception;
    }

    /**
     * Logging action for a {@code CompletionStage} that returns {@link HttpResponse}
     * <p>
     * Note that an error at this layer should be treated as a warning for logging purposes, because network
     * errors are relatively common and should be handled and/or logged at higher layers of the stack.
     * @param httpResponse non-null on success
     * @param t non-null on error
     */
    private void log(HttpResponse<String> httpResponse, Throwable t) {
        if ((httpResponse != null)) {
            log.info("log data string: {}", httpResponse);
        } else {
            log.warn("exception: ", t);
        }
    }

    /**
     * Logging action for a {@code CompletionStage} that returns {@link String}. In the current
     * implementation of this client, that should be a JSON-formatted string.
     * <p>
     * Note that an error at this layer should be treated as a warning for logging purposes, because network
     * errors are relatively common and should be handled and/or logged at higher layers of the stack.
     * @param s non-null on success
     * @param t non-null on error
     */
    private void log(String s, Throwable t) {
        if ((s != null)) {
            log.info("log data string: {}", s.substring(0 ,Math.min(100, s.length())));
        } else {
            log.warn("exception: ", t);
        }
    }

    private JavaType responseTypeFor(Class<?> resultType) {
        return mapper.getTypeFactory().
                constructParametricType(JsonRpcResponse.class, resultType);
    }
}
