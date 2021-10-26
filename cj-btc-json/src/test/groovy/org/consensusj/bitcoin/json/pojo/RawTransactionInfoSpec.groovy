package org.consensusj.bitcoin.json.pojo

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionConfidence
import spock.lang.Specification

/**
 * RawTransactionInfo tests
 */
class RawTransactionInfoSpec extends Specification {
    
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

    def "Construct from Mock BitcoinJ transaction"() {
        given:
        def tx = Mock(Transaction) {
            bitcoinSerialize() >> ([0x01] as byte[])
            getConfidence() >> Mock(TransactionConfidence)
            getInputs() >> []
            getOutputs() >> []
        }

        when:
        def raw = new RawTransactionInfo(tx)

        then:
        raw != null
    }
}
