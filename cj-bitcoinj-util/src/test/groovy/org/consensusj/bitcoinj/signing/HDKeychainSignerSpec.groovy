package org.consensusj.bitcoinj.signing

import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.base.ScriptType
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.Wallet
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain

/**
 *
 */
class HDKeychainSignerSpec extends DeterministicKeychainBaseSpec {
    static final Sha256Hash input_txid = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final int input_vout = 0;
    static final Coin input_amount = Coin.SATOSHI;

    void "Can sign a simple Tx"(String netId, ScriptType scriptType) {
        given: "Given a deterministic seed, a keychain, and some test addresses"
        NetworkParameters netParams = NetworkParameters.fromID(netId)
        DeterministicSeed seed = setupTestSeed();

        BipStandardDeterministicKeyChain keychain = new BipStandardDeterministicKeyChain(seed, scriptType, netParams);
        println("DeterministicKeyChain.accountPath = ${keychain.getAccountPath()}")
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        keychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 1)  // Generate first receiving address
        keychain.getKeys(KeyChain.KeyPurpose.CHANGE, 1)         // Generate first change address

        Address fromAddress = keychain.receivingAddr(0)
        Address toAddress = Address.fromKey(netParams, new ECKey(), scriptType)
        Address changeAddress = keychain.changeAddr(0)

        when: "We we create an HDKeychainSigner"
        HDKeychainSigner signer = new HDKeychainSigner(keychain);

        and: "We sign a transaction"
        SigningRequest signingRequest = new DefaultSigningRequest(netParams)
                .addInput(fromAddress, input_amount, input_txid, input_vout)
                .addOutput(toAddress, 0.01.btc)
                .addOutput(changeAddress, 0.20990147.btc)
        Transaction signedTx = signer.signTransaction(signingRequest).join()

        then:
        signedTx != null
        signedTx.verify()

        when: "We validate the signature on the input"
        Address addressFromInput = signingRequest.inputs().get(0).address().orElse(null)
        correctlySpendsInput(signedTx, 0, addressFromInput)

        then: "It validates successfully"
        noExceptionThrown()

        where:
        netId                       | scriptType
        "org.bitcoin.test"          | ScriptType.P2PKH
        "org.bitcoin.production"    | ScriptType.P2PKH
        "org.bitcoin.test"          | ScriptType.P2WPKH
        "org.bitcoin.production"    | ScriptType.P2WPKH
    }


    void "Can sign a simple Tx with keys from wallet"() {
        given: "Given a deterministic seed, a keychain, and some test addresses"
        var walletFile = new File("src/test/resources/bip44_testnet_panda.wallet")
        var wallet = Wallet.loadFromFile(walletFile)
        var netParams = wallet.getNetworkParameters()
        var scriptType = wallet.getActiveKeyChain().getOutputScriptType()
        var c = wallet.getActiveKeyChain()
        println("DeterministicKeyChain.accountPath = ${c.getAccountPath()}")
        var keychain = new BipStandardDeterministicKeyChain(c, netParams)
        println("DeterministicKeyChain.accountPath = ${keychain.getAccountPath()}")

        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        keychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 1)  // Generate first receiving address
        keychain.getKeys(KeyChain.KeyPurpose.CHANGE, 1)         // Generate first change address

        Address fromAddress = keychain.receivingAddr(0)
        Address toAddress = Address.fromKey(netParams, new ECKey(), scriptType)
        Address changeAddress = keychain.changeAddr(0)

        when: "We we create an HDKeychainSigner"
        HDKeychainSigner signer = new HDKeychainSigner(keychain);

        and: "We sign a transaction"
        SigningRequest signingRequest = new DefaultSigningRequest(netParams)
                .addInput(fromAddress, input_amount, input_txid, input_vout)
                .addOutput(toAddress, 0.01.btc)
                .addOutput(changeAddress, 0.20990147.btc)
        Transaction signedTx = signer.signTransaction(signingRequest).join()

        then:
        signedTx != null
        signedTx.verify()

        when: "We validate the signature on the input"
        Address addressFromInput = signingRequest.inputs().get(0).address().orElse(null)
        correctlySpendsInput(signedTx, 0, addressFromInput)

        then: "It validates successfully"
        noExceptionThrown()
    }

    void "Verify addresses in test wallet"() {
        given: "Given a deterministic seed, a keychain, and some test addresses"
        var walletFile = new File("src/test/resources/bip44_testnet_panda.wallet")
        var wallet = Wallet.loadFromFile(walletFile)
        var netParams = wallet.getNetworkParameters()
        var scriptType = wallet.getActiveKeyChain().getOutputScriptType()
        var c = wallet.getActiveKeyChain()
        println("DeterministicKeyChain.accountPath = ${c.getAccountPath()}")
        var keychain = new BipStandardDeterministicKeyChain(c, netParams)
        println("DeterministicKeyChain.accountPath = ${keychain.getAccountPath()}")

        when:
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        keychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 10)  // Generate first receiving address
        keychain.getKeys(KeyChain.KeyPurpose.CHANGE, 10)         // Generate first change address

        then:
        keychain.getAccountPath() == HDPath.parsePath("m/44H/1H/0H");

        then:
        keychain.receivingAddr(0) == address("muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak")

        keychain.changeAddr(0) == address("muerkyvAYxuDRwvodNXmjg8UFP8wFaUWB8")

        and:
        keychain.findKeyFromPubHash(Address.fromString(null, "muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak").getHash()) != null
        keychain.findKeyFromPubHash(Address.fromString(null, "muerkyvAYxuDRwvodNXmjg8UFP8wFaUWB8").getHash()) != null
    }

    private Address address(String addressString) {
        return Address.fromString(null, addressString);
    }
}
