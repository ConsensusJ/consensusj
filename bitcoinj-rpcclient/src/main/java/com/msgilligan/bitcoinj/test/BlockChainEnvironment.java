package com.msgilligan.bitcoinj.test;

import org.bitcoinj.core.Sha256Hash;

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
    List<Sha256Hash> waitForBlocks(long numBlocks) throws Exception;

}
