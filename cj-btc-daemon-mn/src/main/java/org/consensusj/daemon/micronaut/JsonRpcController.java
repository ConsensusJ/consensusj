package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;

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
                PropertyNamingStrategy.UpperCamelCaseStrategy.class,
                ArrayList.class,
                LinkedHashMap.class,
                HashSet.class,
                JsonRpcRequest.class,
                JsonRpcResponse.class,
                BitcoinImpl.class,
                ServerInfo.class
        },
        accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_DECLARED_METHODS}
)
@Controller("/rpc")
public class JsonRpcController {
    private final JsonRpcService jsonRpcService;

    public JsonRpcController() {
        jsonRpcService = new BitcoinImpl();
    }

    @Post(produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<JsonRpcResponse<Object>> index(@Body JsonRpcRequest req) {
        return jsonRpcService.call(req);
    }

}
