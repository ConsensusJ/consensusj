package org.consensusj.daemon.micronaut;

import io.micronaut.context.annotation.Context;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Create a REST endpoint from a JsonRpcService
 * Here we're using Micronaut annotations to wrap the JsonRpcService
 * in an HTTP environment and to serialize JSON to and from Java POJOs.
 */
@Controller("/")
@Context
public class JsonRpcController {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcController.class);
    private final JsonRpcService jsonRpcService;

    public JsonRpcController(WalletAppKitService walletAppKitService) {
        log.info("Constructing JsonRpcController");
        jsonRpcService = walletAppKitService;
    }

    @Post(produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<JsonRpcResponse<Object>> index(@Body JsonRpcRequest req) {
        log.info("JSON-RPC call: {}", req.getMethod());
        return jsonRpcService.call(req);
    }

}
