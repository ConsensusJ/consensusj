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

import org.bitcoinj.base.Coin
import org.consensusj.bitcoin.json.pojo.bitcore.AddressBalanceInfo
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.IgnoreIf
import spock.lang.Requires

/**
 * Test of OmniCore Bitcore address index JSON-RPC method: {@code getaddressbalance}
 * If {@code help} reports address index is not available, these tests are ignored.
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class GetAddressBalanceSpec extends BaseRegTestSpec {

    @Requires({ instance.isAddressIndexEnabled()})
    def "get 1 address balance"() {
        given:
        def address = client.getNewAddress()
        client.generateToAddress(1, address)

        when:
        AddressBalanceInfo balanceInfo = client.getAddressBalance(address)

        then:
        balanceInfo != null
        balanceInfo.balance >= Coin.ZERO
        balanceInfo.received >= Coin.ZERO
        balanceInfo.immature >= Coin.ZERO

    }

    @Requires({ instance.isAddressIndexEnabled()})
    def "get multi-address balance"() {
        given:
        def address1 = client.getNewAddress()
        def address2 = client.getNewAddress()
        client.generateToAddress(1, address1)
        client.generateToAddress(1, address2)

        when:
        AddressBalanceInfo balanceInfo = client.getAddressBalance([address1, address2])

        then:
        balanceInfo != null
        balanceInfo.balance >= Coin.ZERO
        balanceInfo.received >= Coin.ZERO
        balanceInfo.immature >= Coin.ZERO
    }
}
