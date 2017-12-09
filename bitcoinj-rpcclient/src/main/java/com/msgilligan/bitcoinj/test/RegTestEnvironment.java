package com.msgilligan.bitcoinj.test;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.consensusj.jsonrpc.JsonRPCException;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.util.List;

/**
 *  Single-threaded RegTest testing environment.
 *  (It is conceivable someone could write tests that run in parallel in RegTest mode
 *  just as they would on MainNet or Testnet. There would be a timer that creates blocks
 *  every 'n' seconds, so tests could run in parallel, but would complete much quicker.
 */
public class RegTestEnvironment implements BlockChainEnvironment {
    private BitcoinClient client;

    public RegTestEnvironment(BitcoinClient client) {
        this.client = client;
    }

    @Override
    public List<Sha256Hash> waitForBlock() throws Exception {
        return waitForBlocks(1);
    }

    @Override
    public List<Sha256Hash> waitForBlocks(int numBlocks) throws JsonRPCException, IOException {
        return client.generate(numBlocks);
    }
}
