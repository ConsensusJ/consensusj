package org.consensusj.bitcoinj.spock

import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.Wallet
import spock.lang.Specification

/**
 *
 */
class CreateWalletSpec extends Specification {
    def "quick test"() {
        when:
        def wallet = Wallet.createDeterministic(MainNetParams.get(), Script.ScriptType.P2WPKH)

        then:
        wallet != null
    }
}
