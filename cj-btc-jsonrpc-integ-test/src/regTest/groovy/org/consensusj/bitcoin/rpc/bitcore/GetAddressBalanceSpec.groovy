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
