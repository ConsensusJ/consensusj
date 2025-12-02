package org.consensusj.bitcoin.rpc.tx

import org.bitcoinj.base.Coin
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptPattern
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Non-P2SH multisig - Fund and redeem a bare multisig output
 * Based on https://bitcoinj.org/working-with-contracts
 * We have modified the example slightly to adapt it into a @Stepwise Spock test.
 * Each "feature" test corresponds to a section of the example. The example does not
 * specify the exact communication between the 'client' and 'server' and in some cases
 * provides calls to unimplemented methods like `sendToClientApp(signature)`.
 * In this Specification we're using the @Stepwise annotation and @Shared variables to
 * communicate between the "client" and the "server"
 */
@Stepwise
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class BareMultisigSpec extends TxTestBaseSpec {

    private static final ECKey clientKey = new ECKey();
    private static final ECKey serverKey = new ECKey();
    private static final Coin amount = 9.999.btc
    private static final Coin amount2 = amount - 0.001.btc

    @Shared
    Transaction contract

    @Shared
    TransactionSignature serverSignature

    def "create and send a bitcoinj bare multisig transaction"() {
        given: "transaction ingredients (and keys and amount in static finals)"
        // ingredients is a test-fixture that is like a micro-wallet that can be used
        // to fund transactions
        def ingredients = createIngredients(10.btc)

        when: "we build a transaction"
        contract = new Transaction()

        // 2-of-2 multisig
        // Note that at this point it is not necessary to have the private key for either/any of the keys.
        // In the bitcoinj example code it shows the server key being created from a variable called `publicKeyBytes`
        Script script = ScriptBuilder.createMultiSigOutputScript(2, [clientKey, serverKey])
        contract.addOutput(amount, script)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        contract.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), null, ingredients.privateKey)

        and: "send via P2P and generate a block"
        Transaction sentTx = submitRPC(contract)

        then: "the new address has a balance of amount"
        sentTx != null
        // What else can we verify here?
    }

    def "server-side signing"() {
        when: "we receive the transaction"
        // The example says:
        // Assume we get the multisig transaction we're trying to spend from
        // somewhere, like a network connection.
        // In the Spock test we get it from the contract shared variable

        TransactionOutput multisigOutput = contract.getOutput(0)
        Script multisigScript = multisigOutput.getScriptPubKey()
        Coin value = multisigOutput.getValue()

        then: "it's a multisig output and the amount is correct"
        ScriptPattern.isSentToMultisig(multisigScript)
        value == amount

        when: "OK, now build a transaction that spends the money back to the client."
        Transaction spendTx = new Transaction();
        spendTx.addOutput(amount2, clientKey)
        spendTx.addInput(multisigOutput)

        and: "we sign the transaction"
        Sha256Hash sighash = spendTx.hashForSignature(0,                    // index of input to sign
                multisigScript,         // redeem script
                Transaction.SigHash.ALL,// hash type
                false)                 // anyone can spend?
        serverSignature = new TransactionSignature(serverKey.sign(sighash), Transaction.SigHash.ALL, false);
        // In the bitcoinj example the server signature is sent to the client with an unspecified
        // sendToClientApp(signature) method, in this Spock test it is sent via a @Shared variable

        then:
        serverSignature != null
        // What else can we verify here?
    }

    def "client-side signing and spending"() {
        given:
        TransactionOutput multisigOutput = contract.getOutput(0)

        when: "we build a transaction"
        Transaction spendTx = new Transaction()
        spendTx.addOutput(amount2, clientKey)
        TransactionInput input = spendTx.addInput(multisigOutput)
        Sha256Hash sighash = spendTx.hashForSignature(0,
                multisigOutput.getScriptPubKey(),
                Transaction.SigHash.ALL,
                false)
        TransactionSignature mySignature = new TransactionSignature(clientKey.sign(sighash), Transaction.SigHash.ALL, false)

        and: "a multisig input script"
        // Create the script that spends the multi-sig output.
        Script inputScript = ScriptBuilder.createMultiSigInputScript([mySignature, serverSignature])
        // Replace the unsigned input (placeholder) with the signed input
        TransactionInput signedInput = input.withScriptSig(inputScript)
        spendTx.replaceInput(0, signedInput)

        and: "signed input verifies"
        // We can now check the server provided signature is correct, of course...
        signedInput.verify(multisigOutput)  // Throws an exception if the script doesn't run.

        and: "we send it via P2P"
        // It's valid! Let's take back the money.
        Transaction confirmedTx = submitRPC(spendTx)

        then:
        // [Client] now has the money back in it.
        confirmedTx.outputs.size() == 1
        ScriptPattern.isP2PK(confirmedTx.outputs[0].getScriptPubKey())
        confirmedTx.outputs[0].value == amount2
    }
}