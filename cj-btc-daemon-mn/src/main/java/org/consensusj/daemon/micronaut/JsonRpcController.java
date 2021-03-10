package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
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
                ServerInfo.class,
                BlockInfo.class,
                BlockChainInfo.class,
                Sha256Hash.class,
                WalletAppKitService.class
        },
        accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_DECLARED_METHODS}
)
@Controller("/jsonrpc")
@Context
public class JsonRpcController {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcController.class);
    private final JsonRpcService jsonRpcService;

    public JsonRpcController(WalletAppKitJsonRpcService walletAppKitService) {
        log.info("Constructing JsonRpcController");
        jsonRpcService = walletAppKitService;
    }

    @Post(produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<JsonRpcResponse<Object>> index(@Body JsonRpcRequest req) {
        log.info("JSON-RPC call: {}", req.getMethod());
        return jsonRpcService.call(req);
    }

}
