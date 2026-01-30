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
package org.consensusj.bitcoin.rpc.bitcore

import org.consensusj.bitcoin.json.pojo.bitcore.AddressUtxoInfo
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.IgnoreIf
import spock.lang.Requires

/**
 * Test of OmniCore Bitcore address index JSON-RPC method: {@code getaddressutxos}
 * If {@code help} reports address index is not available, these tests are ignored.
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class GetAddressUtxosSpec extends BaseRegTestSpec  {
    @Requires({ instance.isAddressIndexEnabled()})
    def "get utxo info"() {
        given:
        def address = client.getNewAddress()
        client.generateToAddress(1, address)

        when:
        List<AddressUtxoInfo> utxoInfoList = client.getAddressUtxos(address)

        then:
        utxoInfoList != null
        utxoInfoList.size() == 1
    }
}
