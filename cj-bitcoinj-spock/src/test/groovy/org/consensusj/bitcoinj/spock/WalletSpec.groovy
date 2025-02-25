package org.consensusj.bitcoinj.spock

import org.bitcoinj.base.Address
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.Network
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.wallet.Wallet
import spock.lang.Shared
import spock.lang.Specification

import static org.bitcoinj.base.BitcoinNetwork.MAINNET

/**
 * Basic tests of wallet serialization/deserialization
 */
class WalletSpec  extends Specification {
    static final Address roAddress = AddressParser.getDefault(MAINNET).parseAddress( "1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa");

    @Shared
    Network network
    @Shared
    Wallet wallet


    void setupSpec() {
        network = roAddress.network()
        wallet = newEmptyWallet()
    }

    def "create new wallet, add watched address"() {
        given:
        List<Address> addresses = [roAddress]
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4_000_000)

        when:
        wallet.addWatchedAddresses(addresses)

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
        wallet = Wallet.createDeterministic(network, ScriptType.P2PKH)
    }
}
