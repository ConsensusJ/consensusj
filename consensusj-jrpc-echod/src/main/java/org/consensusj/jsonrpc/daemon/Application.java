package org.consensusj.jsonrpc.daemon;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.consensusj.jsonrpc.services.EchoJsonRpcService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Factory
public class Application {
    static final String DEFAULT_APPNAME = "server";
    private static final Logger log = LoggerFactory.getLogger(org.consensusj.jsonrpc.daemon.Application.class);

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @Singleton
    @Bean(preDestroy = "close")
    EchoJsonRpcService echoService(ApplicationContext applicationContext, EmbeddedServer embeddedServer) {
        return new EchoJsonRpcService(() -> {
            String appName = embeddedServer.getApplicationConfiguration().getName().orElse(DEFAULT_APPNAME);
            applicationContext.stop();  // Shut down the application context which shuts down the embedded server
            return appName + " stopping";
        });
    }

    @Controller("/")
    public static class JsonRpcController {
        @Inject JsonRpcService jsonRpcService;

        @Post(produces = MediaType.APPLICATION_JSON)
        public CompletableFuture<JsonRpcResponse<Object>> index(@Body JsonRpcRequest request) {
            log.debug("JSON-RPC call: {}", request.getMethod());
            return jsonRpcService.call(request);
        }
    }

    @ServerWebSocket("/ws")
    public static class EchoWebSocket {
        @Inject JsonRpcService jsonRpcService;

        @OnMessage
        public Publisher<JsonRpcResponse<Object>> onMessage(JsonRpcRequest request, WebSocketSession session) {
            return session.send(jsonRpcService.call(request).join());
        }
    }
}
