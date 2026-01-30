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
package org.consensusj.bitcoinj.dsl.groovy.categories

import org.bitcoinj.base.Address
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import spock.lang.Specification

import static org.bitcoinj.base.BitcoinNetwork.MAINNET

/**
 * Test specification for ECKey static extension methods
 */
class StaticECKeyExtensionSpec extends Specification {
    static final private AddressParser addressParser = AddressParser.getDefault(MAINNET);
    // WIF for private key used in Bitcoins the Hard Way
    static final fromKeyWIF = "5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD"

    // Expected P2PKH address for test WIF
    static final expectedAddress = addressParser.parseAddress("1MMMMSUb1piy2ufrSguNUdFmAcvqrQF8M5")
    static final expectedSegWitAddress = addressParser.parseAddress("bc1qqgde67hj65u4vcpfrhxq8mjemr05n2kklx68g4")

    def "Can create key and address from private key WIF"() {
        when: "we create a private key from WIF format string in the article"
        ECKey fromKey = ECKey.fromWIF(fromKeyWIF, false)

        and: "we convert it to an address"
        Address fromAddress = fromKey.toAddress(ScriptType.P2PKH, MAINNET)

        then: "it is the address from the article"
        fromAddress == expectedAddress
    }

    def "Can create key and segwit address from private key WIF"() {
        when: "we create a private key from WIF format string in the article"
        ECKey fromKey = ECKey.fromWIF(fromKeyWIF, true)

        and: "we convert it to a segwit address"
        Address fromAddress = fromKey.toAddress(ScriptType.P2WPKH, MAINNET)

        then: "it is as expected"
        fromAddress == expectedSegWitAddress
    }
}
