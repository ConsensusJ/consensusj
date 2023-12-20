package org.consensusj.decentralizedid.btcr

import spock.lang.Specification

/**
 *  Tests for ExtendedTxRef
 */
class ExtendedTxRefSpec extends Specification {
    
    def "Can decode (slightly modified) Bech32 TxRef string with bitcoinj" () {
        when:
        def txRef = ExtendedTxRef.of("xkyt-fzgq-qq87-xnhn")

        then:
        txRef != null
        txRef.bech32.hrp == "txtest"
        txRef.bech32.bytes() == [6, 22, 4, 11, 9, 2, 8, 0, 0, 0] as byte[]
    }

}
