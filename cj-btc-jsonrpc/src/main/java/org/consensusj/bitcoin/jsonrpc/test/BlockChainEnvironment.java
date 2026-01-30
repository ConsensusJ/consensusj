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
package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Sha256Hash;

import java.util.List;

/**
 * Abstraction(s) to help reuse tests between RegTest mode and TestNet or MainNet
 * RegTest mode implementation will generate blocks when waitForBlocks() is called.
 * Other implementations will actually wait for blocks to be confirmed.
 */
public interface BlockChainEnvironment {
    /**
     * Wait for (and possible *hasten*) the creation of a single block
     *
     * @return A list with a single block hash
     */
    List<Sha256Hash> waitForBlock() throws Exception;

    /**
     * Wait for (and possible *hasten*) the creation of blocks
     *
     * @param numBlocks the number of blocks to wait for
     * @return A list of block hashes
     */
    List<Sha256Hash> waitForBlocks(int numBlocks) throws Exception;
}
