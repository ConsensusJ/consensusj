package com.msgilligan.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BriefLogFormatter
import spock.lang.Shared
import spock.lang.Specification

/**
 * Basic tests of wallet serialization/deserialization
 */
class WalletSpec  extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final Address roAddress = Address.fromString(mainNetParams, "1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa")

    @Shared
    NetworkParameters params
    @Shared
    Wallet wallet


    void setupSpec() {
        wallet = newEmptyWallet()
    }

    def "create new wallet, add watched address"() {
        given:
        List<Address> addresses = [roAddress]
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4_000_000)

        when:
        wallet.addWatchedAddresses(addresses, 0)

        and:
        wallet.saveToFileStream(stream)
        ByteArrayInputStream input = new ByteArrayInputStream(stream.toByteArray())

        and:
        def walletCopy = Wallet.loadFromFileStream(input)
        def outAddresses = walletCopy.watchedAddresses

        then:
        outAddresses.size() == 1
        outAddresses[0] == addresses[0]
    }

    def "create new wallet, add key"() {
        given:
        List<ECKey> keys = [new ECKey()]
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4_000_000)

        when:
        wallet.importKeys(keys)
        and:
        wallet.saveToFileStream(stream)
        ByteArrayInputStream input = new ByteArrayInputStream(stream.toByteArray())

        and:
        def walletCopy = Wallet.loadFromFileStream(input)
        def outKeys = walletCopy.getImportedKeys()

        then:
        outKeys.size() == 1
        outKeys[0] == keys[0]
    }

    Wallet newEmptyWallet() {
        wallet = new Wallet(mainNetParams)
    }
}
