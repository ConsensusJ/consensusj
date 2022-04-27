package org.consensusj.jsonrpc.introspection;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class TrivialJsonRpcService {
    public CompletableFuture<Integer> getblockcount() {
        return CompletableFuture.completedFuture(99);
    }
}
