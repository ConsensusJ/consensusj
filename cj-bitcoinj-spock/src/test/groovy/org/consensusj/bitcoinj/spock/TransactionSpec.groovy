package org.consensusj.bitcoinj.spock

import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.Script

import spock.lang.Specification

/**
 * Create and verify the transaction from Ken Shirriff's blog article
 * See "Bitcoins the Hard Way"
 * http://www.righto.com/2014/02/bitcoins-hard-way-using-raw-bitcoin.html
 */
class TransactionSpec extends Specification {
    static final mainNetParams = MainNetParams.get()

    // Input Values
    static final ECKey fromKey = ECKey.fromWIF("5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD", false)
    static final Address toAddr = Address.fromString(mainNetParams, "1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa")
    static final Sha256Hash utxo_id = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final Coin txAmount = 0.00091234.btc

    // Values used for Verification
    static final fromAddrVerify = Address.fromString(mainNetParams, "1MMMMSUb1piy2ufrSguNUdFmAcvqrQF8M5")

    def "Can create and serialize a transaction"() {

        when: "We create a signed transaction"
        Address fromAddress = Address.fromKey(mainNetParams, fromKey, ScriptType.P2PKH)
        Transaction tx = new Transaction(mainNetParams)
        TransactionOutPoint outPoint = new TransactionOutPoint(mainNetParams, 0, utxo_id)
        tx.addOutput(txAmount, toAddr)
        tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddress), fromKey);

        and: "We serialize the transaction"
        byte[] rawTx = tx.bitcoinSerialize()

        and: "We parse it into a new Transaction object"
        Transaction parsedTx = new Transaction(mainNetParams, rawTx)

        then: "Parsed transaction is as expected"
        // We can't do a byte-by-byte comparison because there is a random component to the signature

        parsedTx.version == 1

        parsedTx.inputs.size() == 1

        with (parsedTx.inputs.get(0)) {
            outpoint.hash == utxo_id
            outpoint.index == 0
            scriptBytes.length == 0x8a
            scriptSig != null       // script needs to be broken down more and compared here
            sequenceNumber == 0xffffffff
        }

        parsedTx.outputs.size() == 1

        with (parsedTx.outputs.get(0)) {
            value == txAmount
            scriptBytes.length == 0x19
            scriptPubKey != null    // script needs to be broken down more and compared here
        }

        parsedTx.lockTime == 0

        when: "We validate the signature on each input"
        def inputs = parsedTx.getInputs();
        boolean validSig = false     // Only 1 input for now
        for(int i = 0; i < inputs.size(); i++) {
            TransactionInput input = inputs.get(i);
            Script scriptSig = input.getScriptSig();
            Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddress);
            scriptSig.correctlySpends(parsedTx, i, scriptPubKey, Script.ALL_VERIFY_FLAGS);
        }

        then: "Signature is valid"
        noExceptionThrown()
    }
}
