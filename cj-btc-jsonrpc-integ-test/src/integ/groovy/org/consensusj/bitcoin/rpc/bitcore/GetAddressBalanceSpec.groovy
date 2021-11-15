package org.consensusj.bitcoin.rpc.bitcore

import org.bitcoinj.core.Coin
import org.consensusj.bitcoin.json.pojo.bitcore.AddressBalanceInfo
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.jsonrpc.JsonRpcException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Shared

/**
 * Test of OmniCore Bitcore address index JSON-RPC method: {@code getaddressbalance}
 * If {@code help} reports this method is not available, these tests are ignored.
 */
class GetAddressBalanceSpec extends BaseRegTestSpec {
    @Shared
    boolean hasMethod

    @Requires({ instance.hasMethod})
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

    @Requires({ instance.hasMethod})
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

    def setupSpec() {
        hasMethod = client.commandExists("getaddressbalance")
    }

}
