package org.consensusj.jsonrpc.daemon;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.consensusj.jsonrpc.services.EchoJsonRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Create a REST endpoint from a JsonRpcService
 * Here we're using Micronaut annotations to wrap the JsonRpcService
 * in an HTTP environment and to serialize JSON to and from Java POJOs.
 */
@TypeHint(
        value = {
                PropertyNamingStrategies.UpperCamelCaseStrategy.class,
                ArrayList.class,
                LinkedHashMap.class,
                HashSet.class,
                JsonRpcRequest.class,
                JsonRpcResponse.class,
                EchoJsonRpcService.class
        },
        accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_DECLARED_METHODS}
)
@Controller("/")
public class JsonRpcController {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcController.class);
    private final JsonRpcService jsonRpcService;

    public JsonRpcController(JsonRpcService jsonRpcService) {
        log.info("Constructing JsonRpcController using {}", jsonRpcService);
        this.jsonRpcService = jsonRpcService;
    }

    @Post(produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<JsonRpcResponse<Object>> index(@Body JsonRpcRequest request) {
        log.debug("JSON-RPC call: {}", request.getMethod());
        return jsonRpcService.call(request);
    }
}
