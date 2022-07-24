package org.consensusj.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bouncycastle.util.encoders.Hex
import spock.lang.Ignore
import spock.lang.Specification


class ECKeySpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final testNetParams = TestNet3Params.get()
    static final regTestParams = RegTestParams.get()
    static final BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"))

    def "Generate a new, random valid Elliptic Curve Keypair"() {
        when: "We randomly generate a 256-bit private key and paired public key"
        def key = new ECKey()

        then: "it is a valid keypair"
        key.hasPrivKey()                    // Private key is present
        !key.pubKeyOnly                     // Yes, we have both. Really.
        key.privKeyBytes.length == 256/8    // is 256 bits (32 bytes) long
        key.encryptedPrivateKey == null     // This key has not been encrypted with a passphrase
        key.pubKey != null                  // Raw public key value as appears in scriptSigs
        // pubKey is 33 bytes when compressed, otherwise 66 bytes
        (key.pubKey.length == 33 && key.compressed) ||
        (key.pubKey.length == 65 && !key.compressed)
//        key.pubKeyCanonical                 // Canonical makes sure length is right for compressed/uncompressed
        ECKey.isPubKeyCanonical(key.pubKey) // Length is correct for compressed/uncompressed
        key.pubKeyHash.length == 20         // Is available in RIPEMD160 form
        // Can be converted to addresses (which have a different header for each network
        // This test is no longer directly testing the header becuase of changes in bitcoinj 0.15
        Address.fromKey(mainNetParams, key, Script.ScriptType.P2PKH).parameters == mainNetParams
        Address.fromKey(testNetParams, key, Script.ScriptType.P2PKH).parameters == testNetParams
        Address.fromKey(regTestParams, key, Script.ScriptType.P2PKH).parameters == regTestParams
        key.creationTimeSeconds > 0          // since we created it, we know the creation time
    }

    def "Import a constant, publicly-known private key "() {
        when: "We import a constant, publicly known public key"
        def key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

        then:
        key.toString() == "ECKey{pub HEX=0401de173aa944eacf7e44e5073baca93fb34fe4b7897a1c82c92dfdc8a1f75ef58cd1b06e8052096980cb6e1ad6d3df143c34b3d7394bae2782a4df570554c2fb, isEncrypted=false, isPubKeyOnly=false}"
//        key.toString() == "ECKey{pub=0401de173aa944eacf7e44e5073baca93fb34fe4b7897a1c82c92dfdc8a1f75ef58cd1b06e8052096980cb6e1ad6d3df143c34b3d7394bae2782a4df570554c2fb, isEncrypted=false}"
        key.pubKey.encodeHex().toString() == "0401de173aa944eacf7e44e5073baca93fb34fe4b7897a1c82c92dfdc8a1f75ef58cd1b06e8052096980cb6e1ad6d3df143c34b3d7394bae2782a4df570554c2fb"
        key.pubKey.encodeBase64().toString() == "BAHeFzqpROrPfkTlBzusqT+zT+S3iXocgskt/cih9171jNGwboBSCWmAy24a1tPfFDw0s9c5S64ngqTfVwVUwvs="
    }

    def "Use a key for signing and verifying messages"() {
        // TBD
    }
}
