package org.consensusj.jsonrpc;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous JSON-RPC Server/Service
 *
 * A service-layer abstraction of JSON-RPC using plain old Java objects (POJOs)
 * Can easily be used in controllers from Java Web frameworks.
 *
 */
public interface JsonRpcService {
    /**
     * Handle a JSON-RPC Request POJO and return a Response POJO
     * @param req The Request POJO
     * @return the Response POJO
     */
    <RSLT> CompletableFuture<JsonRpcResponse<RSLT>> call(JsonRpcRequest req);
}
