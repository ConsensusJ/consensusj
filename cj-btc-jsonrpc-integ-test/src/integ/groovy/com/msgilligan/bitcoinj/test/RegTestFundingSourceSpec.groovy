package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.BaseRegTestSpec
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import spock.lang.Shared

/**
 * Test the fundingSource created by BaseRegTestSpec
 */
class RegTestFundingSourceSpec extends BaseRegTestSpec {
    @Shared
    RegTestFundingSource source;

    def "smoke"() {
        expect:
        source != null
    }

    def "we can consolidate coins"() {
        when:
        source.consolidateCoins()

        then:
        noExceptionThrown()
    }

    def "we can fund an address"() {
        given:
        Address requestor = client.getNewAddress()

        when:
        Sha256Hash txid = source.requestBitcoin(requestor, Coin.CENT)
        def info = client.getTransaction(txid)
        
        then:
        info.confirmations == 0

        when:
        client.generateBlocks(1)
        info = client.getTransaction(txid)
        def balance = client.getBitcoinBalance(requestor)

        then:
        info.confirmations == 1
        balance == Coin.CENT
    }

    def setupSpec() {
        // Load our own variable so we can cast it to the right type
        source = (RegTestFundingSource) fundingSource;
    }

}
