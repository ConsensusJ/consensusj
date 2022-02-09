package org.consensusj.bitcoin.dsl.groovy.categories

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script
import spock.lang.Specification

/**
 * Test specification for ECKey static extension methods
 */
class StaticECKeyExtensionSpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    // WIF for private key used in Bitcoins the Hard Way
    static final fromKeyWIF = "5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD"

    // Expected P2PKH address for test WIF
    static final expectedAddress = Address.fromString(mainNetParams, "1MMMMSUb1piy2ufrSguNUdFmAcvqrQF8M5")
    
    def "Can create key and address from private key WIF"() {
        when: "we create a private key from WIF format string in the article"
        ECKey fromKey = ECKey.fromWIF(fromKeyWIF)

        and: "we convert it to an address"
        Address fromAddress = Address.fromKey(mainNetParams, fromKey, Script.ScriptType.P2PKH)

        then: "it is the address from the article"
        fromAddress == expectedAddress
    }
}
