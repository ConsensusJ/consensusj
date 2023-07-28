package org.consensusj.bitcoin.json.pojo

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Context
import org.bitcoinj.testing.FakeTxBuilder
import spock.lang.Specification

/**
 * RawTransactionInfo tests
 */
class RawTransactionInfoSpec extends Specification {
    def setupSpec() {
        Context.propagate(new Context());
    }
    
    def "Jackson-style constructor works"() {
        when:
        def raw = new RawTransactionInfo("FF",
                                Sha256Hash.ZERO_HASH,
                                1,
                                1,
                                null,
                                null,
                                Sha256Hash.ZERO_HASH,
                                0,
                                0,
                                0)

        then:
        raw != null
        raw.version == 1
    }

    def "Construct from Fake BitcoinJ transaction"() {
        given:
        def tx = FakeTxBuilder.createFakeTx(BitcoinNetwork.MAINNET)

        when:
        def raw = new RawTransactionInfo(tx)

        then:
        raw != null
    }
}
