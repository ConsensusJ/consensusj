package org.consensusj.bitcoin.json.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import org.bitcoinj.base.Coin;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.bitcoinj.base.Sha256Hash;

import java.math.BigInteger;
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
// TODO: Support Sha256Hash type in request params: see Issue #104
//  CompletableFuture<Object> getblock(Sha256Hash blockHash, Integer verbosity);
    CompletableFuture<JsonNode> getblock(String blockHashString, Integer verbosity);
    CompletableFuture<Sha256Hash> getblockhash(Integer blockNumber);
    CompletableFuture<Integer> getconnectioncount();
    CompletableFuture<BlockChainInfo> getblockchaininfo();
    CompletableFuture<NetworkInfo> getnetworkinfo();
    CompletableFuture<String> getnewaddress();
    CompletableFuture<String> getbalance();
    CompletableFuture<Sha256Hash> sendtoaddress(String address, Double amount);
}
