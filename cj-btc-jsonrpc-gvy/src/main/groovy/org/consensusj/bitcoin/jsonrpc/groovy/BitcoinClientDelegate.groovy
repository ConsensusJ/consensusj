package org.consensusj.bitcoin.jsonrpc.groovy

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient

/**
 * Trait to Mix-in BitcoinClient methods via Delegation pattern
 */
trait BitcoinClientDelegate {
    @Delegate
    BitcoinExtendedClient client

    /**
     * Since we can't have a final (read-only) property in a trait
     * Let's at least require it to be only set once.
     */
    synchronized void setClient(BitcoinExtendedClient client) {
        if (this.client == null) {
            this.client = client
        } else {
            throw new RuntimeException("Client already set")
        }
    }
}