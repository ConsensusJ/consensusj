package org.consensusj.jsonrpc;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for an Asynchronous JSON-RPC Server/Service.
 * <p>
 * A service-layer abstraction of JSON-RPC using plain old Java objects (POJOs)
 * Can easily be used in controllers from Java Web frameworks.
 *
 */
public interface JsonRpcService {
    /**
     * Handle a JSON-RPC {@link JsonRpcRequest} and return a {@link JsonRpcResponse} POJO
     * @param req A Request object
     * @param <RSLT> Generic type for the JSON-RPC <b>result</b> in {@link JsonRpcResponse#getResult()}
     * @return a future for a Response object with either a <b>result</b> or an error.
     */
    <RSLT> CompletableFuture<JsonRpcResponse<RSLT>> call(JsonRpcRequest req);
}
