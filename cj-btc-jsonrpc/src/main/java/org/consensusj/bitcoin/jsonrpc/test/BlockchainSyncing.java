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

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Wait for synchronization with a reference source of block height.
 *
 * Since synchronization may take time, we check the block height again
 * after waitForBlock returns.
 */
public interface BlockchainSyncing {
    Logger log = LoggerFactory.getLogger(BlockchainSyncing.class);

    /**
     * Wait until a {@link BitcoinClient} is synced to a reference blockheight
     * @param client The client we want to synchronize
     * @return the blockheight upon synchronization
     */
    default int waitForSync(BitcoinClient client) throws IOException {
        //
        // Get in sync with the block chain
        //
        int curHeight = 0;
        int newHeight = getReferenceBlockHeight();
        log.info("Reference current height: {}", newHeight);
        while ( newHeight > curHeight ) {
            curHeight = newHeight;
            Boolean upToDate = client.waitForBlock(curHeight, 60*60);
            newHeight = getReferenceBlockHeight();
            log.info("Current reference block height: {}", newHeight);
        }
        return curHeight;
    }

    /**
     * Use an external reference to get the current block height
     * See: BlockchainDotInfoSyncing
     */
    int getReferenceBlockHeight();
}
