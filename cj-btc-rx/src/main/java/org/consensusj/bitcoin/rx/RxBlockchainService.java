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
package org.consensusj.bitcoin.rx;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.reactivestreams.Publisher;

import java.io.Closeable;

/**
 * Reactive Streams interface for subscribing to reactive blockchain data.
 *
 * There are at least 3 possible implementations
 * 1. A bitcoinj {@link org.bitcoinj.core.PeerGroup}
 * 2. The ZeroMQ (and JSON-RPC) service of a trusted Bitcoin Core node
 * 3. A Bitcoin web service using WebSocket
 *
 * Note: Implementation instances may throw {@link UnsupportedOperationException} if they don't support a particular
 * published data type.
 */
public interface RxBlockchainService extends Closeable {
    Publisher<Transaction> transactionPublisher();
    Publisher<Sha256Hash> transactionHashPublisher();
    Publisher<Block> blockPublisher();
    Publisher<Sha256Hash> blockHashPublisher();
    Publisher<Integer> blockHeightPublisher();
    Publisher<ChainTip> chainTipPublisher();
}
