package org.consensusj.bitcoinj.spock

import org.bitcoinj.base.Address
import org.bitcoinj.base.LegacyAddress
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.SegwitAddress
import org.bitcoinj.crypto.ECKey
import spock.lang.Ignore
import spock.lang.Specification

import static org.bitcoinj.base.BitcoinNetwork.MAINNET
import static org.bitcoinj.base.BitcoinNetwork.REGTEST
import static org.bitcoinj.base.BitcoinNetwork.TESTNET

class AddressSpec extends Specification {
    static final NotSoPrivatePrivateKey = new BigInteger(1, "180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19".decodeHex())
    static final key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

    def "Create valid MainNet Address from private key"() {

        when: "We create a MainNet Address"
        Address address = key.toAddress(ScriptType.P2PKH, MAINNET)

        then: "It has expected value and properties"
        address.toString() == "1GtCqbyqTzbvtBWMMRgkwkxenPJNzz1TY4"
        address instanceof LegacyAddress
        ((LegacyAddress) address).version == MAINNET.legacyAddressHeader()
        address.network() == MAINNET
    }

    def "Create valid TestNet Address from private key"() {

        when: "We create a TestNet Address"
        Address address = key.toAddress(ScriptType.P2PKH, TESTNET)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address instanceof LegacyAddress
        ((LegacyAddress) address).version == TESTNET.legacyAddressHeader()
        address.network() == TESTNET
    }

    def "Create valid RegTest Address from private key"() {

        when: "We create a RegTest Address"
        Address address = key.toAddress(ScriptType.P2PKH, REGTEST)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address instanceof LegacyAddress
        ((LegacyAddress) address).version == TESTNET.legacyAddressHeader()
        address.network() == TESTNET
    }

    def "MainNet and TestNet3 address from same key aren't equals and don't compare"() {

        when: "We create a ainNet and TestNet3 addresses"
        Address mainNetAddr = key.toAddress(ScriptType.P2PKH, MAINNET)
        Address testNetAddr = key.toAddress(ScriptType.P2PKH, TESTNET)

        then: "They aren't equals() and don't compareTo()"
        !mainNetAddr.equals(testNetAddr)
        mainNetAddr.compareTo(testNetAddr) != 0

        and: "Their STRINGS DO MATCH"
        mainNetAddr.toString() != testNetAddr.toString()
    }

    def "TestNet3 and Regtest address from same key ARE equals and DO compare"() {

        when: "We create a ainNet and TestNet3 addresses"
        Address testNetAddr = key.toAddress(ScriptType.P2PKH, TESTNET)
        Address regTestAddr = key.toAddress(ScriptType.P2PKH, REGTEST)

        then: "They ARE equals() and DO compareTo()"
        testNetAddr.equals(regTestAddr)
        testNetAddr.compareTo(regTestAddr) == 0

        and: "Their STRINGS ACTUALLY DO MATCH"
        testNetAddr.toString() == regTestAddr.toString()
    }

    def "Create a Segwit address"() {
        given:
        var key = new ECKey();

        when:
        Address segAddress = key.toAddress(ScriptType.P2WPKH, MAINNET)

        then:
        segAddress != null
        segAddress instanceof SegwitAddress

    }

    @Ignore
    def "Create a Taproot address"() {
        given:
        var key = new ECKey();

        when:
        Address tapAddress = key.toAddress(ScriptType.P2TR, MAINNET)

        then:
        tapAddress != null
    }

    @Ignore
    def "x"() {
        when:
        var key = ECKey.fromPrivate(new BigInteger("1000"))
        var a = key.toAddress(ScriptType.P2TR, MAINNET).toString()

        then:
        a != null
    }
}
