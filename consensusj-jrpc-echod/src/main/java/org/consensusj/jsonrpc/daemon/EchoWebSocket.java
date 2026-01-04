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

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket controller that serves {@link EchoJsonRpcService} on a WebSocket endpoint.
 */
@ServerWebSocket("/ws")
public class EchoWebSocket {
    private static final Logger log = LoggerFactory.getLogger(EchoWebSocket.class);
    private final WebSocketBroadcaster broadcaster;
    private final JsonRpcService jsonRpcService;

    public EchoWebSocket(WebSocketBroadcaster broadcaster, JsonRpcService jsonRpcService) {
        this.broadcaster = broadcaster;
        this.jsonRpcService = jsonRpcService;
    }

    @OnOpen
    public void onOpen(WebSocketSession session) {
    }

    @OnMessage
    public Publisher<JsonRpcResponse<Object>> onMessage(JsonRpcRequest request, WebSocketSession session) {
        JsonRpcResponse<Object> response = jsonRpcService.call(request).join();
        return session.send(response);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
    }
}
