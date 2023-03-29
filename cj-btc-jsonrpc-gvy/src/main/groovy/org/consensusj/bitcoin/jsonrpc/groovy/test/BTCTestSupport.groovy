package org.consensusj.bitcoin.jsonrpc.groovy.test

import groovy.transform.CompileStatic
import org.bitcoinj.base.Network
import org.consensusj.bitcoin.jsonrpc.test.BitcoinClientAccessor

// TODO: Migrate to Java
/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
@CompileStatic
interface BTCTestSupport extends BitcoinClientAccessor {

    /**
     * Wait for the server to become ready and validate the Bitcoin network it is running on
     * @param expectedNetworkParams The network the server is expected to be running on
     */
    default void serverReady(Network expectedNetwork) {
        Boolean ready = client().waitForServer(60);   // Wait up to 1 minute
        if (!ready) {
            throw new RuntimeException("Timeout waiting for server");
        }
        // As soon as the server is ready we must initialize network parameters in the client (by querying them)
        Network network = client().getNetwork();
        assert network.equals(expectedNetwork);
    }
}
