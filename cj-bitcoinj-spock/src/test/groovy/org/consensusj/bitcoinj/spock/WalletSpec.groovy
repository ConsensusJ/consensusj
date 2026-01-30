/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
