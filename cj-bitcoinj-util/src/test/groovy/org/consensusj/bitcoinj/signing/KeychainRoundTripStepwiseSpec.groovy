package org.consensusj.bitcoinj.signing

import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Network
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain
import spock.lang.Shared
import spock.lang.Stepwise

import java.time.Instant

/**
 * Test Specification for the round-trip process of signing a Bitcoin Testnet transaction with
 * a paired NETWORK (xpub-based, pubkey-only) and SIGNING keychains.
 * <p>
 * The {code @Stepwise} annotation on this test specification means that the feature methods
 * are run in order and can communicate with each other via the {@code @Shared} variables.
 */
@Stepwise
class KeychainRoundTripStepwiseSpec extends DeterministicKeychainBaseSpec  {
    // Account path for the network wallet
    static final HDPath networkAccountPath = HDPath.M(ChildNumber.ZERO_HARDENED)
    // Relative paths to keys used in tests
    static final HDPath fromKeyPath = HDPath.M(ChildNumber.ONE, ChildNumber.ZERO)
    static final HDPath toKeyPath = HDPath.M(ChildNumber.ZERO, ChildNumber.ONE)
    static final HDPath changeKeyPath = HDPath.M(ChildNumber.ONE, ChildNumber.ONE)

    // Parameters and Keychain initialized in setupSpec
    @Shared Network network
    @Shared BipStandardDeterministicKeyChain signingKeychain

    // Variables used to communicate between test steps
    @Shared String xpub
    @Shared Instant xpubCreationInstant
    @Shared DeterministicKeyChain networkKeyChain
    @Shared SigningRequest signingRequest
    @Shared Transaction signedTx

    /**
     * Setup test parameters and create the signingKeyChain
     */
    def setupSpec() {
        network = BitcoinNetwork.TESTNET
        ScriptType outputScriptType = ScriptType.P2PKH;
//        org.bitcoinj.base.ScriptType outputScriptType = ScriptType.P2WPKH;
        DeterministicSeed seed = setupTestSeed()

        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, network);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 2)  // Generate first 2 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 2)         // Generate first 2 change address
    }

    def "SIGNING wallet can create an xpub string from signing keychain"() {
        expect: "Test setup provides a DeterministicKeyChain initialized with the Panda Diary seed"
        signingKeychain != null

        when:
        DeterministicKey watchingKey = signingKeychain.getWatchingKey()
        xpub = watchingKey.serializePubB58(network)
        xpubCreationInstant = watchingKey.creationTime().orElseThrow(RuntimeException::new)

        then:
        xpub.length() > 0
        xpub == "tpubDDpSwdfCsfnYP8SH7YZvu1LK3BUMr3RQruCKTkKdtnHy2iBNJWn1CYvLwgskZxVNBV4KhicZ4FfgFCGjTwo4ATqdwoQcb5UjJ6ejaey5Ff8"
    }

    def "NETWORK wallet can create a network keychain from the xpub"() {
        given:
        ScriptType outputScriptType = signingKeychain.getOutputScriptType()
        HDPath signingAccountPath = signingKeychain.getAccountPath()

        when: "we create a network keychain from the xpub"
        DeterministicKey key = DeterministicKey.deserializeB58(xpub, network)
        key.setCreationTime(xpubCreationInstant);
        networkKeyChain = DeterministicKeyChain.builder().watch(key).outputScriptType(outputScriptType).build()

        and: "we fetch the keys that are used in later steps"
        DeterministicKey fromKey = networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(fromKeyPath), true)
        DeterministicKey toKey = networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(toKeyPath), true)
        DeterministicKey changeKey = networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(changeKeyPath), true)

        then: "the pubkeys in the network keychain match the pubkeys in the signing keychain"
        networkKeyChain != null
        networkKeyChain.isWatching()
        fromKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.m(signingAccountPath).extend(fromKeyPath), false).getPubKey()
        toKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.m(signingAccountPath).extend(toKeyPath), false).getPubKey()
        changeKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.m(signingAccountPath).extend(changeKeyPath), false).getPubKey()
    }

    def "NETWORK wallet can create a transaction and create a signing request "() {
        given: "a transaction with a UTXO in output 1"
        boolean isSegwit = signingKeychain.getOutputScriptType() == ScriptType.P2WPKH
        Script script
        Coin utxoAmount
        Sha256Hash txid
        int index
        if (isSegwit) {
            // Make believe transaction
            script = ScriptBuilder.createP2WPKHOutputScript(signingKeychain.getKeyByPath(HDPath.m(signingKeychain.getAccountPath()).extend(fromKeyPath), false))
            utxoAmount = Coin.CENT
            txid = Sha256Hash.ZERO_HASH
            index = 0
        } else {
            // This is actually the first transaction received by the 0'th change address in our "panda diary" keychain.
            Transaction parentTx = firstChangeTransaction()
            TransactionOutput utxo = parentTx.getOutput(1)
            script = utxo.scriptPubKey
            utxoAmount = utxo.value
            txid = parentTx.txId
            index = utxo.index
        }

        when: "we build a 1-input, 2-output (unsigned) transaction request to spend the UTXO"
        Address toAddr = signingKeychain.addressFromKey(networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(toKeyPath), false))
        Address changeAddr = signingKeychain.addressFromKey(networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(changeKeyPath), false))
        Coin txAmount = 0.01.btc
        Coin changeAmount = 0.20990147.btc
        signingRequest = new DefaultSigningRequest(network)
                .addInput(script, utxoAmount, txid, index)
                .addOutput(toAddr, txAmount)
                .addOutput(changeAddr, changeAmount)

        then: "request looks correct"
        signingRequest != null
        // TODO: More checks
    }

    def "SIGNING wallet can sign and produce a valid transaction"() {
        given: "An transaction signer object"
        HDKeychainSigner signer = new HDKeychainSigner(signingKeychain)

        when: "we sign the transaction"
        signedTx = signer.signTransaction(signingRequest).get()

        then:
        signedTx != null
    }

    def "The signed transaction verifies"() {
        expect: "it verifies"
        signedTx.verify()

        when: "We validate the signature on the input"
        // Extract fromAddress out of the signing request
        var signingKey = signingKeychain.getKeyByPath(HDPath.m(signingKeychain.getAccountPath()).extend(fromKeyPath), false);
        var fromAddress = signingKey.toAddress(signingKeychain.getOutputScriptType(), network);
        TransactionVerification.correctlySpendsInput(signedTx, 0, fromAddress)

        then: "It validates successfully"
        noExceptionThrown()
    }
}
