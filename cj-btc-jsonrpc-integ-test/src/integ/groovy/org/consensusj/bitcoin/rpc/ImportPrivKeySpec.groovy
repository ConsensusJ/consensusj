package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoin.json.pojo.AddressInfo
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.Script
import spock.lang.Stepwise

/**
 * Functional test of importPrivKey
 */
@Stepwise
class ImportPrivKeySpec extends BaseRegTestSpec  {
    static final ECKey TEST_PRIVATE_KEY = new ECKey().decompress()
    static final Address TEST_ADDRESS = Address.fromKey(RegTestParams.get(), TEST_PRIVATE_KEY, Script.ScriptType.P2PKH)
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
