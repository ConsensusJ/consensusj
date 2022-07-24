package org.consensusj.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import spock.lang.Ignore
import spock.lang.Specification

class AddressSpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final testNetParams = TestNet3Params.get()
    static final regTestParams = RegTestParams.get()
    static final NotSoPrivatePrivateKey = new BigInteger(1, "180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19".decodeHex())
    static final key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

    def "Create valid MainNet Address from private key"() {

        when: "We create a MainNet Address"
        Address address = Address.fromKey(mainNetParams, key, Script.ScriptType.P2PKH)

        then: "It has expected value and properties"
        address.toString() == "1GtCqbyqTzbvtBWMMRgkwkxenPJNzz1TY4"
        address.version == mainNetParams.addressHeader
        address.parameters == mainNetParams
    }

    def "Create valid TestNet Address from private key"() {

        when: "We create a TestNet Address"
        Address address = Address.fromKey(testNetParams, key, Script.ScriptType.P2PKH)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address.version == testNetParams.addressHeader
        address.parameters == testNetParams
    }

    def "Create valid RegTest Address from private key"() {

        when: "We create a RegTest Address"
        Address address = Address.fromKey(regTestParams, key, Script.ScriptType.P2PKH)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address.version == regTestParams.addressHeader
        address.parameters == regTestParams
    }

    def "MainNet and TestNet3 address from same key aren't equals and don't compare"() {

        when: "We create a ainNet and TestNet3 addresses"
        Address mainNetAddr = Address.fromKey(mainNetParams, key, Script.ScriptType.P2PKH)
        Address testNetAddr = Address.fromKey(testNetParams, key, Script.ScriptType.P2PKH)

        then: "They don't equals() or compareTo()"
        !mainNetAddr.equals(testNetAddr)
        mainNetAddr.compareTo(testNetAddr) != 0

        and: "Their STRINGS DON'T MATCH"
        mainNetAddr.toString() != testNetAddr.toString()
    }

    def "TestNet3 and Regtest address from same key aren't equals and don't compare"() {

        when: "We create a ainNet and TestNet3 addresses"
        Address mainNetAddr = Address.fromKey(testNetParams, key, Script.ScriptType.P2PKH)
        Address testNetAddr = Address.fromKey(regTestParams, key, Script.ScriptType.P2PKH)

        then: "They don't equals() or compareTo()"
        !mainNetAddr.equals(testNetAddr)
        mainNetAddr.compareTo(testNetAddr) != 0

        and: "Their STRINGS ACTUALLY DO MATCH"
        mainNetAddr.toString() == testNetAddr.toString()
    }

    def "Create a Segwit address"() {
        given:
        var key = new ECKey();

        when:
        Address segAddress = Address.fromKey(mainNetParams, key, Script.ScriptType.P2WPKH)

        then:
        segAddress != null

    }

    @Ignore
    def "Create a Taproot address"() {
        given:
        var key = new ECKey();

        when:
        Address tapAddress = Address.fromKey(mainNetParams, key, Script.ScriptType.P2TR)

        then:
        tapAddress != null
    }

    @Ignore
    def "x"() {
        when:
        var key = ECKey.fromPrivate(new BigInteger("1000"))
        var a = SegwitAddress.fromKey(MainNetParams.get(), key, ScriptType.P2TR).toString()

        then:
        a != null
    }
}
