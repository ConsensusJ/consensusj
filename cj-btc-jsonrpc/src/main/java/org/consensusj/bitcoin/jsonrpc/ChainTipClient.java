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
