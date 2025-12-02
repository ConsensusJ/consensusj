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
