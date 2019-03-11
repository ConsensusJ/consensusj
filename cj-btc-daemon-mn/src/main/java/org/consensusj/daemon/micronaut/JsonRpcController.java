package org.consensusj.daemon.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.introspection.JsonRpcServerWrapper;
import org.consensusj.jsonrpc.introspection.JsonRpcServerWrapperImpl;

import java.util.concurrent.CompletableFuture;

/**
 * Create a REST endpoint from a JsonRpcHandler
 */
@Controller("/")
public class JsonRpcController {
    private JsonRpcServerWrapper handler;

    public JsonRpcController() {
        handler = new JsonRpcServerWrapperImpl(new BitcoinImpl());
    }

    @Post(produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<JsonRpcResponse<?>> index(JsonRpcRequest req) {
        return handler.call(req);
    }

}
