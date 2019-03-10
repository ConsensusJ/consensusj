package com.msgilligan.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
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

}
