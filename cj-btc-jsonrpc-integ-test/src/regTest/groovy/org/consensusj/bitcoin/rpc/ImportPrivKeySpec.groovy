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
package org.consensusj.bitcoin.rpc

import org.bitcoinj.base.ScriptType
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.json.pojo.AddressInfo
import org.bitcoinj.base.Address
import org.bitcoinj.crypto.ECKey
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Stepwise

import static org.bitcoinj.base.BitcoinNetwork.REGTEST

/**
 * Functional test of importPrivKey
 */
@Stepwise
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class ImportPrivKeySpec extends BaseRegTestSpec  {
    static final ECKey TEST_PRIVATE_KEY = new ECKey().decompress()
    static final Address TEST_ADDRESS = TEST_PRIVATE_KEY.toAddress(ScriptType.P2PKH, REGTEST)
    static final String TEST_ADDRESS_LABEL = "ImportPrivKeySpec";
    
    def "can import without error"() {
        when:
        client.importPrivKey(TEST_PRIVATE_KEY, TEST_ADDRESS_LABEL, false)

        then:
        noExceptionThrown()
    }

    def "can correctly retrieve key that equals stored key"() {
        when:
        ECKey dumpedKey = client.dumpPrivKey(TEST_ADDRESS)

        then:
        dumpedKey.equals(TEST_PRIVATE_KEY)
    }

    def "addressinfo returns correct information for key's address"() {
        when:
        AddressInfo addressInfo = client.getAddressInfo(TEST_ADDRESS)

        then:
        addressInfo.address == TEST_ADDRESS
    }
}
