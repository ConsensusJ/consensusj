package org.consensusj.bitcoin.jsonrpc.test;

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;

/**
 * Interface for tests that use a BitcoinExtendedClient
 */
public interface BitcoinClientAccessor {

    /**
     * Preferred accessor
     * @return The Bitcoin Client
     */
    BitcoinExtendedClient client();

    /**
     * JavaBeans style getter/accessor (for Groovy, etc)
     * @return The Bitcoin Client
     */
    default BitcoinExtendedClient getClient() {
        return client();
    }
}
