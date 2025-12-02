package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.base.Coin
import spock.lang.IgnoreIf

/**
 * Test Spec for BTCTestSupport.
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
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