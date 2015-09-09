package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
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
        generateBlock()

        then:
        getBitcoinBalance(requestingAddress).btc == requestedAmount
    }
}