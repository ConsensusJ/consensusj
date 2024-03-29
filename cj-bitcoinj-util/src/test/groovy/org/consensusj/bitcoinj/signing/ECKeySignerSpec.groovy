package org.consensusj.bitcoinj.signing

import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.base.ScriptType

/**
 *
 */
class ECKeySignerSpec extends DeterministicKeychainBaseSpec {
    // WIF for private key used in Bitcoins the Hard Way
    static final ECKey fromKey = ECKey.fromWIF("5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD", true)
    static final Sha256Hash input_txid = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final Utxo utxo = Utxo.of(input_txid, 0, Coin.SATOSHI);

    def "Can sign a simple Tx"(BitcoinNetwork network, ScriptType scriptType) {
        given:
        Address fromAddress = fromKey.toAddress(scriptType, network)
        Address toAddress = new ECKey().toAddress(scriptType, network)
        Address changeAddress = new ECKey().toAddress(scriptType, network)

        when: "We we create an ECKeySigner"
        ECKeySigner signer = new ECKeySigner(fromKey)

        and: "We sign a transaction"
        SigningRequest signingRequest = SigningRequest.of(
                [TransactionInputData.of(utxo, fromAddress)],
                [(toAddress): 0.01.btc, (changeAddress): 0.20990147.btc])

        Transaction signedTx = signer.signTransaction(signingRequest).join()

        then:
        signedTx != null
        Transaction.verify(network, signedTx)

        when: "We validate the signature on the input"
        TransactionVerification.correctlySpendsInput(signedTx, 0, fromAddress)

        then: "It validates successfully"
        noExceptionThrown()

        where:
        network                 | scriptType
        BitcoinNetwork.MAINNET  | ScriptType.P2PKH
        BitcoinNetwork.TESTNET  | ScriptType.P2PKH
        BitcoinNetwork.MAINNET  | ScriptType.P2WPKH
        BitcoinNetwork.TESTNET  | ScriptType.P2WPKH
    }
}
