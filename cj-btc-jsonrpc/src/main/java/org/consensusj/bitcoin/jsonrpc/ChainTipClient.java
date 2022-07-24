package org.consensusj.bitcoin.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.jsonrpc.JsonRpcStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Interface for a Bitcoin JSON-RPC client that implements {@code getchaintips}.
 * <p>
 * (This is needed to provide reactive Bitcoin JSON-RPC interfaces since we can't
 * use BitcoinClient itself because it is concrete. In the future we may want to create
 * interfaces with more Bitcoin RPC methods, but for now this is all we need.)
 */
public interface ChainTipClient {
    List<ChainTip> getChainTips() throws JsonRpcStatusException, IOException;
}
