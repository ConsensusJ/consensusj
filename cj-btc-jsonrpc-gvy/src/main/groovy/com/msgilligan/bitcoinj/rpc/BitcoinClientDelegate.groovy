package com.msgilligan.bitcoinj.rpc

/**
 * Trait to Mix-in BitcoinClient methods via Delegation pattern
 */
trait BitcoinClientDelegate {
    @Delegate
    BitcoinExtendedClient client

    /**
     * Since we can't have a final (read-only) property in a trait
     * Let's at least allow it to be only set once.
     */
    void setClient(BitcoinExtendedClient client) {
        if (this.client == null) {
            this.client = client
        } else {
            throw new RuntimeException("Client already set")
        }
    }
}