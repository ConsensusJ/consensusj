/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoinj.spock.tx


import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes
import org.bitcoinj.script.Script
import spock.lang.Unroll

import java.nio.ByteBuffer


/**
 * Test/Demonstration of OP_RETURN transaction
 */
class OpReturnSpec extends BaseTransactionSpec {

    @Unroll
    def "Can create and serialize an OP_RETURN transaction of #length bytes"(int length) {
        given: "32 bytes of test data"
        //def testData = Sha256Hash.of("abcd".decodeHex()).bytes;
        def testData = 0..<length as byte[]

        when: "we build an OP_RETURN transaction"
        Transaction tx = new Transaction()
        TransactionOutPoint outPoint = new TransactionOutPoint( 0, utxo_id)
        Script script = new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(testData)
                .build()
        tx.addOutput(utxo_amount, script)
        tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddr), utxo_amount, fromKey)


        and: "We serialize the transaction"
        byte[] rawTx = tx.serialize()

        and: "We parse it into a new Transaction object"
        Transaction parsedTx = Transaction.read(ByteBuffer.wrap(rawTx))

        then: "we can retrieve the data"
        with (parsedTx.getOutput(0).scriptPubKey.chunks().get(0)) {
            opcode == ScriptOpCodes.OP_RETURN
        }
        with (parsedTx.getOutput(0).scriptPubKey.chunks().get(1)) {
            opcode == opCodeFromLength(testData.length);
            data == testData
        }

        where:
        length << [0, 1, 39, 79, 80]
    }

    int opCodeFromLength(int length) {
        return (length >= ScriptOpCodes.OP_PUSHDATA1) ? ScriptOpCodes.OP_PUSHDATA1 : length
    }
}