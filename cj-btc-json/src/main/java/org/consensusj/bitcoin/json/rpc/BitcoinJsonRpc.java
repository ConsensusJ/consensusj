/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoin.json.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.bitcoinj.base.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Standard Bitcoin JSON-RPC API RPCs. For now, this is a subset implementation that is not fully-compatible
 * with Bitcoin Core. Some parameters are missing and the defined response types may omit some properties.
 * <p>
 * All parameters are object types so {@code null} can be used to represent an omitted optional parameter.
 */
public interface BitcoinJsonRpc {
    int DEFAULT_MIN_CONF = 1;
    int DEFAULT_MAX_CONF = 9999999;

    /* == Blockchain == */

    CompletableFuture<Sha256Hash> getbestblockhash();
    // TODO: Support Sha256Hash type in request params: see Issue #104
//  CompletableFuture<Object> getblock(Sha256Hash blockHash, Integer verbosity);
    CompletableFuture<JsonNode> getblock(String blockHashString, Integer verbosity);
    CompletableFuture<BlockChainInfo> getblockchaininfo();
    CompletableFuture<Integer> getblockcount();
    CompletableFuture<Sha256Hash> getblockhash(Integer blockNumber);
    CompletableFuture<JsonNode> getblockheader(String blockHashString, Boolean verbose);

    /* == Control == */

    CompletableFuture<String> help();
    CompletableFuture<String> stop();

    /* == Network == */

    CompletableFuture<Integer> getconnectioncount();
    CompletableFuture<NetworkInfo> getnetworkinfo();

    /* == Rawtransactions == */

    CompletableFuture<String> createrawtransaction(List<Map<String, Object>> inputs, List<Map<String, String>> outputs);
    CompletableFuture<Sha256Hash> sendrawtransaction(String hex);

    /* == Wallet == */

    CompletableFuture<Coin> getbalance();
    CompletableFuture<Address> getnewaddress();
    CompletableFuture<List<UnspentOutput>> listunspent(Integer minConf, Integer maxConf, List<String> addresses, Boolean includeUnsafe);
    CompletableFuture<Sha256Hash> sendtoaddress(String address, Double amount);

    // TODO: Add prevtxs and sighashtype parameters
    /**
     * Currently all UTXOs must be present in the wallet and {@code sighashtype} defaults to {@code "ALL"}.
     * @param hex hex-encoded unsigned raw transaction
     * @return An object containing the signed transaction
     */
    CompletableFuture<SignedRawTransaction> signrawtransactionwithwallet(String hex);
}
