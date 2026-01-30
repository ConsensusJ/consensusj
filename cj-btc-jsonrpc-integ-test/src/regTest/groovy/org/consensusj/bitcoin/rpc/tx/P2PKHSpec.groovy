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
package org.consensusj.bitcoin.rpc.tx

import org.bitcoinj.base.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.util.function.UnaryOperator

/**
 * Create, send and verify P2PKH transactions via P2P and RPC
 */
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class P2PKHSpec extends TxTestBaseSpec {

    @Unroll
    def "create and send a bitcoinj P2PKH transaction using #methodName"(UnaryOperator<Transaction> submitMethod, String methodName) {
        given: "transaction ingredients, a destination address and an amount to send"
        def ingredients = createIngredients(10.btc)
        // since we're not currently using a change address, RPC calls won't let us
        // spend too much 'absurdly-high-fee' or too little in transaction fees
        // so maybe we should use change addresses in these tests?
        Coin amount = 9.999.btc
        def destAddress = client.getNewAddress()

        when: "we build a transaction"
        Transaction tx = new Transaction()
        tx.addOutput(amount, destAddress)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), null, ingredients.privateKey);

        and: "send via submitMethod [P2P, RPC] and generate a block"
        Transaction sentTx = submitMethod.apply(tx)

        then: "the new address has a balance of amount"
        client.getReceivedByAddress(destAddress) == amount  // Verify destAddress balance

        where: "submitMethod is P2P and then RPC"
        methodItem << submitMethods
        submitMethod = methodItem.method
        methodName = methodItem.name
    }
}
