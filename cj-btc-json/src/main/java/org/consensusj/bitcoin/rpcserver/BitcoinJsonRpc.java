package org.consensusj.bitcoin.rpcserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.bitcoinj.core.Sha256Hash;

import java.util.concurrent.CompletableFuture;

/**
 * Standard Bitcoin JSON-RPC service
 */
public interface BitcoinJsonRpc {
    CompletableFuture<String> help();
    CompletableFuture<String> stop();
    CompletableFuture<Integer> getblockcount();
    CompletableFuture<Sha256Hash> getbestblockhash();
    CompletableFuture<JsonNode> getblockheader(String blockHashString, Boolean verbose);
// TODO: Support Sha256Hash type in request params?
//    Object getblock(Sha256Hash blockHash, Integer verbosity);
    CompletableFuture<JsonNode> getblock(String blockHashString, Integer verbosity);
    CompletableFuture<Sha256Hash> getblockhash(Integer blockNumber);
    CompletableFuture<Integer> getconnectioncount();
    CompletableFuture<BlockChainInfo> getblockchaininfo();
    CompletableFuture<NetworkInfo> getnetworkinfo();
}
