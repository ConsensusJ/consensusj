package org.consensusj.bitcoin.signing

import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script.ScriptType
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain

/**
 *
 */
class HDKeychainSignerSpec extends DeterministicKeychainBaseSpec {
    static final Sha256Hash input_txid = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final int input_vout = 0;
    static final Coin input_amount = Coin.SATOSHI;

    def "Can sign a simple Tx"(NetworkParameters netParams, ScriptType scriptType) {
        given: "Given a deterministic seed, a keychain, and some test addresses"
        DeterministicSeed seed = setupTestSeed();
        int signingAccountIndex = 0;

        BipStandardDeterministicKeyChain keyChain = new BipStandardDeterministicKeyChain(seed, scriptType, netParams, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        keyChain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 1)  // Generate first receiving address
        keyChain.getKeys(KeyChain.KeyPurpose.CHANGE, 1)         // Generate first change address

        Address fromAddress = keyChain.receivingAddr(0)
        Address toAddress = Address.fromKey(netParams, new ECKey(), scriptType)
        Address changeAddress = keyChain.changeAddr(0)

        when: "We we create an HDKeychainSigner"
        HDKeychainSigner signer = new HDKeychainSigner(keyChain);

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
        netParams            | scriptType
        MainNetParams.get()  | ScriptType.P2PKH
        TestNet3Params.get() | ScriptType.P2PKH
        MainNetParams.get()  | ScriptType.P2WPKH
        TestNet3Params.get() | ScriptType.P2WPKH
    }
}
