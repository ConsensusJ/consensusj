package org.consensusj.bitcoin.signing

import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
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
    static final HDPath networkAccountPath = HDPath.of(ChildNumber.ZERO_HARDENED)
    // Relative paths to keys used in tests
    static final HDPath fromKeyPath = HDPath.of(ChildNumber.ONE, ChildNumber.ZERO)
    static final HDPath toKeyPath = HDPath.of(ChildNumber.ZERO, ChildNumber.ONE)
    static final HDPath changeKeyPath = HDPath.of(ChildNumber.ONE, ChildNumber.ONE)

    // Parameters and Keychain initialized in setupSpec
    @Shared NetworkParameters netParams
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
        netParams = TestNet3Params.get()
        int signingAccountIndex = 0
        Script.ScriptType outputScriptType = Script.ScriptType.P2PKH;
        DeterministicSeed seed = setupTestSeed()

        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, netParams, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 2)  // Generate first 2 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 2)         // Generate first 2 change address
    }

    def "SIGNING wallet can create an xpub string from signing keychain"() {
        expect: "Test setup provides a DeterministicKeyChain initialized with the Panda Diary seed"
        signingKeychain != null

        when:
        def watchingKey = signingKeychain.getWatchingKey()
        xpub = watchingKey.serializePubB58(netParams,  signingKeychain.getOutputScriptType())
        xpubCreationInstant = Instant.ofEpochSecond(watchingKey.creationTimeSeconds)

        then:
        xpub.length() > 0
        xpub == "tpubDDpSwdfCsfnYP8SH7YZvu1LK3BUMr3RQruCKTkKdtnHy2iBNJWn1CYvLwgskZxVNBV4KhicZ4FfgFCGjTwo4ATqdwoQcb5UjJ6ejaey5Ff8"
    }

    def "NETWORK wallet can create a network keychain from the xpub"() {
        given:
        Script.ScriptType outputScriptType = signingKeychain.getOutputScriptType()
        HDPath signingAccountPath = signingKeychain.getAccountPath()

        when: "we create a network keychain from the xpub"
        DeterministicKey key = DeterministicKey.deserializeB58(xpub, netParams)
        key.creationTimeSeconds = xpubCreationInstant.epochSecond
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
        // This is actually the first transaction received by the 0'th change address in our "panda diary" keychain.
        Transaction parentTx = firstChangeTransaction()
        TransactionOutput utxo = parentTx.getOutput(1)

        when: "we build a 1-input, 2-output (unsigned) transaction request to spend the UTXO"
        Address toAddr = signingKeychain.addressFromKey(networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(toKeyPath), false))
        Address changeAddr = signingKeychain.addressFromKey(networkKeyChain.getKeyByPath(HDPath.M(networkAccountPath).extend(changeKeyPath), false))
        Coin txAmount = 0.01.btc
        Coin changeAmount = 0.20990147.btc
        
        and: "We serialize it to transaction signing request"
        TransactionInputData input = new TransactionInputData(netParams.id, parentTx.txId.bytes, utxo.index, utxo.scriptBytes)
        List<TransactionInputData> inputs = List.of(input)
        List<TransactionOutputData> outputs = List.of(
                new TransactionOutputAddress(txAmount.value, toAddr),
                new TransactionOutputAddress(changeAmount.value, changeAddr)
        )
        signingRequest = new DefaultSigningRequest(netParams.id, inputs, outputs)

        then: "request looks correct"
        signingRequest != null
        // TODO: More checks
    }

    def "SIGNING wallet can sign and produce a valid transaction"() {
        given: "An transaction signer object"
        SigningWalletKeychain signer = new SigningWalletKeychain(signingKeychain)

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
        Address fromAddress = signingRequest.inputs().get(0).address().orElse(null)
        correctlySpendsInput(signedTx, 0, fromAddress)

        then: "It validates successfully"
        noExceptionThrown()
    }
}
