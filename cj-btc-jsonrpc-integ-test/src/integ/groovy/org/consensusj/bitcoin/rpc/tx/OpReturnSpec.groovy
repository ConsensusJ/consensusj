package org.consensusj.bitcoin.rpc.tx

import org.bitcoinj.base.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.util.function.UnaryOperator

/**
 *  Create, send, record, and retrieve an OP_RETURN transaction
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class OpReturnSpec extends TxTestBaseSpec {
    @Unroll
    def "create and send a bitcoinj OP_RETURN transaction using #methodName"(UnaryOperator<Transaction> submitMethod, String methodName) {
        given: "data for the OP_RETURN, transaction ingredients, a destination address and an amount to send"
        def length = 80
        def testData = 0..<length as byte[]

        def ingredients = createIngredients(10.btc)
        // We're going to spend a lot of (fake) BTC to write these 80 bytes!
        Coin amount = 9.999.btc

        when: "we build a transaction"
        Transaction tx = new Transaction()
        Script script = new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(testData)
                .build()
        tx.addOutput(amount, script)

        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), null, ingredients.privateKey);

        and: "send via submitMethod [P2P, RPC] and generate a block"
        Transaction sentTx = submitMethod.apply(tx)

        then: "we can retrieve and verify the data"
        with (sentTx.getOutput(0).scriptPubKey.chunks().get(0)) {
            opcode == ScriptOpCodes.OP_RETURN
        }
        with (sentTx.getOutput(0).scriptPubKey.chunks().get(1)) {
            opcode == opCodeFromLength(testData.length);
            data == testData
        }

        where: "submitMethod is P2P and then RPC"
        methodItem << submitMethods
        submitMethod = methodItem.method
        methodName = methodItem.name
    }

    private static int opCodeFromLength(int length) {
        return (length >= ScriptOpCodes.OP_PUSHDATA1) ? ScriptOpCodes.OP_PUSHDATA1 : length
    }


}
