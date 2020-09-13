package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.rpc.BitcoinClientDelegate
import org.consensusj.jsonrpc.groovy.Loggable

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait BTCTestSupport implements BitcoinClientDelegate, FundingSourceDelegate, Loggable {

    void serverReady() {
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            log.error "Timeout error."
        }
        assert available
    }

    void consolidateCoins() {
        fundingSource.fundingSourceMaintenance();
    }
}