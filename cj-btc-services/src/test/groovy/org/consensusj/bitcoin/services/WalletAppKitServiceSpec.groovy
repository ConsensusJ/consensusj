package org.consensusj.bitcoin.services

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */
class WalletAppKitServiceSpec extends Specification {
    @Shared
    WalletAppKitService appKitService;

    def setupSpec() {
        appKitService = WalletAppKitService.createTemporary(BitcoinNetwork.REGTEST, ScriptType.P2PKH, "cj-btc-services-unittest")
        appKitService.start()
    }

    def 'loaded correctly'() {
        expect:
        appKitService != null
        appKitService.network() == BitcoinNetwork.REGTEST
    }
}
