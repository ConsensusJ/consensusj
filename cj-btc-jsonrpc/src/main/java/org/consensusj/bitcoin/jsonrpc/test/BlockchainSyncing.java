package org.consensusj.bitcoin.jsonrpc.groovy;

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
