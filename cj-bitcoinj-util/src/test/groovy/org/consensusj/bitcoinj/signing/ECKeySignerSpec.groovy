package org.consensusj.bitcoinj.signing

import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.base.ScriptType

/**
 *
 */
class ECKeySignerSpec extends DeterministicKeychainBaseSpec {
    // WIF for private key used in Bitcoins the Hard Way
    static final ECKey fromKey = ECKey.fromWIF("5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD", true)
    static final Sha256Hash input_txid = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final int input_vout = 0;
    static final Coin input_amount = Coin.SATOSHI;

    def "Can sign a simple Tx"(NetworkParameters netParams, ScriptType scriptType) {
        given:
        Address fromAddress = Address.fromKey(netParams, fromKey, scriptType)
        Address toAddress = Address.fromKey(netParams, new ECKey(), scriptType)
        Address changeAddress = Address.fromKey(netParams, new ECKey(), scriptType)

        when: "We we create an ECKeySigner"
        ECKeySigner signer = new ECKeySigner(netParams, fromKey, scriptType)

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
