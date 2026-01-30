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

import org.reactivestreams.Publisher;

import java.io.Closeable;

/**
 * For lower-level, higher-performance reactive subscriptions to blockchain data where we don't
 * want the overhead of parsing (and instantiating as a tree of objects) each block or transaction.
 * (e.g. a proxy or relay server)
 *
 *  Note: Implementation instances may throw {@link UnsupportedOperationException} if they don't support a particular
 *  stream type.
 */
public interface RxBlockchainBinaryService extends Closeable {
    Publisher<byte[]> transactionBinaryPublisher();
    Publisher<byte[]> transactionHashBinaryPublisher();
    Publisher<byte[]> blockBinaryPublisher();
    Publisher<byte[]> blockHashBinaryPublisher();
}
