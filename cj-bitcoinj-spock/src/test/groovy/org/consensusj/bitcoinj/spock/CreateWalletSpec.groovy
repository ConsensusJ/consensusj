package org.consensusj.bitcoinj.spock

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.wallet.Wallet
import spock.lang.Specification

/**
 *
 */
class CreateWalletSpec extends Specification {
    def "quick test"() {
        when:
        def wallet = Wallet.createDeterministic(NetworkParameters.of(BitcoinNetwork.MAINNET), ScriptType.P2WPKH)

        then:
        wallet != null
    }
}
