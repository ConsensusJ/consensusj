package com.msgilligan.bitcoinj.spock.tx

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes
import org.bitcoinj.script.Script


/**
 * Test/Demonstration of OP_RETURN transaction
 */
class OpReturnSpec extends BaseTransactionSpec {

    def "Can create and serialize an OP_RETURN transaction"() {
        given: "32 bytes of test data"
        def testData = Sha256Hash.of("abcd".decodeHex()).bytes;

        when: "we build an OP_RETURN transaction"
        Transaction tx = new Transaction(mainNetParams)
        TransactionOutPoint outPoint = new TransactionOutPoint(mainNetParams, 0, utxo_id)
        Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(testData).build()
        tx.addOutput(utxo_amount, script)
        tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddr), fromKey)


        and: "We serialize the transaction"
        byte[] rawTx = tx.bitcoinSerialize()

        and: "We parse it into a new Transaction object"
        Transaction parsedTx = new Transaction(mainNetParams, rawTx)

        then: "we can retrieve the data"
        with (parsedTx.getOutput(0).scriptPubKey.chunks.get(0)) {
            opcode == ScriptOpCodes.OP_RETURN
        }
        with (parsedTx.getOutput(0).scriptPubKey.chunks.get(1)) {
            opcode == testData.length
            data == testData
        }
    }
}