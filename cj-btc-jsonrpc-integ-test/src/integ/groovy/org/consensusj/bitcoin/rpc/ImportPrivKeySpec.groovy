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
