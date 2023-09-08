package org.consensusj.bitcoin.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.jsonrpc.JsonRpcStatusException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for a Bitcoin JSON-RPC client that implements {@code getchaintips}.
 * <p>
 * (This is needed to provide reactive Bitcoin JSON-RPC interfaces because we can't
 * subclass BitcoinClient itself (because it is concrete.) In the future we may want to create
 * interfaces with more Bitcoin RPC methods, but for now this is all we need.)
 */
public interface ChainTipClient {
    @Deprecated
    List<ChainTip> getChainTips() throws JsonRpcStatusException, IOException;
    CompletableFuture<List<ChainTip>> getChainTipsAsync();

    // TODO: Consider using Flow.Publisher to provide some reactive publishers directly in BitcoinClient?
    //Flow.Publisher<ChainTip> chainTipFlowPublisher();
}
