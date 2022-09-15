package org.consensusj.bitcoin.jsonrpc.groovy.test

import org.bitcoinj.core.NetworkParameters
import org.consensusj.bitcoin.jsonrpc.groovy.BitcoinClientDelegate

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait BTCTestSupport implements BitcoinClientDelegate, FundingSourceDelegate {

    /**
     * Wait for the server to become ready and validate the Bitcoin network it is running on
     * @param expectedNetworkParams The network the server is expected to be running on
     */
    void serverReady(NetworkParameters expectedNetworkParams) {
        Boolean ready = client.waitForServer(60)   // Wait up to 1 minute
        if (!ready) {
            throw new RuntimeException("Timeout waiting for server")
        }
        NetworkParameters params = client.getNetParams()
        assert params.equals(expectedNetworkParams)
    }

    void consolidateCoins() {
        fundingSource.fundingSourceMaintenance();
    }
}