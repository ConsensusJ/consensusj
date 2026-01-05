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
package org.consensusj.jsonrpc.daemon;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.jspecify.annotations.NonNull;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.awaitility.Awaitility.await;

/// Test based on: [Micronaut Guide](https://guides.micronaut.io/latest/micronaut-websocket-gradle-java.html#test)
@Property(name = "spec.name", value = "WebSocketTest")
@MicronautTest
public class WebSocketTest {
    private static final Logger log = LoggerFactory.getLogger(WebSocketTest.class);

    @Inject
    BeanContext beanContext;

    @Inject
    EmbeddedServer embeddedServer;


    @Requires(property = "spec.name", value = "WebSocketTest")
    @ClientWebSocket
    static abstract class TestWebSocketClient implements AutoCloseable {

        private final Deque<JsonRpcResponse<Object>> messageHistory = new ConcurrentLinkedDeque<>();

        public JsonRpcResponse<Object> getLatestMessage() {
            return messageHistory.peekLast();
        }

        public List<JsonRpcResponse<Object>> getMessagesChronologically() {
            return new ArrayList<>(messageHistory);
        }

        @OnMessage
        void onMessage(JsonRpcResponse<Object> message) {
            log.info("Response: {}", message.getId());
            messageHistory.add(message);
        }

        abstract void send(@NonNull @NotBlank JsonRpcRequest request);
    }

    private TestWebSocketClient createWebSocketClient(int port) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri = UriBuilder.of("ws://localhost")
                .port(port)
                .path("ws")
                .build();
        Publisher<TestWebSocketClient> client = webSocketClient.connect(TestWebSocketClient.class,  uri);
        return Flux.from(client).blockFirst();
    }

    @Test
    void testWebsocketServerEcho() throws Exception {
        String expectedEcho = "hello world!";

        // Create a WebSocket client
        TestWebSocketClient client = createWebSocketClient(embeddedServer.getPort());

        // Send an echo request to the server
        JsonRpcRequest request = new JsonRpcRequest("echo", List.of(expectedEcho));
        client.send(request);

        // Wait for and verify the echo response
        await().until(() ->
                client.getLatestMessage() != null &&
                expectedEcho.equals(client.getLatestMessage().getResult()));

        // Close the client
        client.close();
    }
}
