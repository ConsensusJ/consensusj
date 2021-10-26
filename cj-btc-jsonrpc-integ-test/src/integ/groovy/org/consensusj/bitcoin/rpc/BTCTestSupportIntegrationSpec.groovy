package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.core.Coin

/**
 * Test Spec for BTCTestSupport.
 */
class BTCTestSupportIntegrationSpec extends BaseRegTestSpec {

    def "we can request newly-mined bitcoins"() {
        given:
        def requestingAddress = newAddress
        Coin requestedAmount = 1.btc

        when:
        requestBitcoin(requestingAddress, requestedAmount)

        and:
        generateBlocks(1)

        then:
        getBitcoinBalance(requestingAddress) == requestedAmount
    }
}