package org.consensusj.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

// TODO: Should this be combined with JsonRpcClientJavaNet? (There is a lot of common code, maybe switch on URI type?)
// TODO: Support SSL (wss)
// TODO: Long-lived websocket connection, rather than connect and disconnect for each request/response
// TODO: Handle notifications (needs persistent connections) -- use j.u.c.Flow?
// TODO: Handle authentication other than Minecraft's bearer-token
/**
 * Proof-of-concept WebSocket JSON-RPC transport
 */
public class JsonRpcClientWebSocket implements JsonRpcTransport<JavaType> {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcClientWebSocket.class);

    private final ObjectMapper mapper;
    private final URI serverURI;
    private final String bearerToken;
    private final HttpClient client;

    public JsonRpcClientWebSocket(ObjectMapper mapper, URI server, String bearerToken) {
        if (!server.getScheme().equals("ws")) {
            throw new IllegalArgumentException("ws only");
        }
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.mapper = mapper;
        this.serverURI = server;
        this.bearerToken = bearerToken;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

    }

    @Override
    public URI getServerURI() {
        return serverURI;
    }

    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        String requestString;
        try {
            requestString = encodeJsonRpcRequest(request);
        } catch (JsonProcessingException e) {
            // TODO: Return this as a failed future
            throw new RuntimeException(e);
        }

        CompletableFuture<String> responseFuture = new CompletableFuture<>();

        WebSocket.Listener listener = new WebSocket.Listener() {
            private final StringBuilder messageBuilder = new StringBuilder();

            @Override
            public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                messageBuilder.append(data);
                if (last) {
                    responseFuture.complete(messageBuilder.toString());
                }
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                responseFuture.completeExceptionally(error);
            }

            @Override
            public CompletableFuture<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(
                            new RuntimeException("WebSocket closed: " + statusCode + " - " + reason)
                    );
                }
                return CompletableFuture.completedFuture(null);
            }
        };

        WebSocket webSocket = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + bearerToken)
                .buildAsync(serverURI, listener)
                .join();

        CompletableFuture<WebSocket> cf = webSocket.sendText(requestString, true);
        // cf completes when the request is sent. TODO: check result

        return responseFuture
                .thenApply(mappingFuncFor(responseType));
    }

    private String encodeJsonRpcRequest(JsonRpcRequest request) throws JsonProcessingException {
        return mapper.writeValueAsString(request);
    }

    // return a MappingFunction for a given type
    private <R, T extends Type> JsonRpcClientJavaNet.MappingFunction<R> mappingFuncFor(T responseType) {
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
}
