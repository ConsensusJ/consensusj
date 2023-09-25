package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Network;
import org.consensusj.jsonrpc.JsonRpcException;

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
public interface BTCTestSupport extends BitcoinClientAccessor {

    /**
     * Wait for the server to become ready and validate the Bitcoin network it is running on
     * @param expectedNetwork The network the server is expected to be running on
     */
    default void serverReady(Network expectedNetwork) throws JsonRpcException {
        Boolean ready = client().waitForServer(60);   // Wait up to 1 minute
        if (!ready) {
            throw new RuntimeException("Timeout waiting for server");
        }
        // As soon as the server is ready we must initialize network parameters in the client (by querying them)
        Network network = client().getNetwork();
        if (!network.equals(expectedNetwork)) {
            throw new IllegalStateException("Server reports unexpected Bitcoin network.");
        }
    }
}
