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

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.jsonrpc.JsonRpcException;
import org.bitcoinj.base.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 *  Single-threaded RegTest testing environment.
 *  (It is conceivable someone could write tests that run in parallel in RegTest mode
 *  just as they would on MainNet or Testnet. There would be a timer that creates blocks
 *  every 'n' seconds, so tests could run in parallel, but would complete much quicker.
 */
public class RegTestEnvironment implements BlockChainEnvironment {
    private static final Logger log = LoggerFactory.getLogger(RegTestEnvironment.class);
    private final BitcoinExtendedClient client;

    public RegTestEnvironment(BitcoinExtendedClient client) {
        this.client = client;
    }

    @Override
    public List<Sha256Hash> waitForBlock() throws Exception {
        return waitForBlocks(1);
    }

    @Override
    public List<Sha256Hash> waitForBlocks(int numBlocks) throws JsonRpcException, IOException {
        return client.generateBlocks(numBlocks);
    }
}
