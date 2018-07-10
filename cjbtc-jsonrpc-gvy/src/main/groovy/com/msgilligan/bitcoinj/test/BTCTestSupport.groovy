package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.rpc.BitcoinClientDelegate
import org.consensusj.jsonrpc.groovy.Loggable
import com.msgilligan.bitcoinj.json.conversion.BitcoinMath
import org.bitcoinj.core.Coin

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait BTCTestSupport implements BitcoinClientDelegate, FundingSourceDelegate, Loggable {
    // TODO: set, or get and verify default values of the client
//    final Coin stdTxFee = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE
    final Coin stdTxFee = 0.00010000.btc   // == 10 *  Transaction.REFERENCE_DEFAULT_MIN_TX_FEE !!!
    final Coin stdRelayTxFee = 0.00001000.btc
    final Integer defaultMaxConf = 9999999

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

    @Deprecated
    Coin btcToCoin(final BigDecimal btc) {
        return BitcoinMath.btcToCoin(btc)
    }
}