package com.msgilligan.bitcoinj.rpc.tx

import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Unroll

/**
 * Create, send and verify P2PKH transactions via P2P and RPC
 */
class P2PKHSpec extends TxTestBaseSpec {

    @Unroll
    def "create and send a bitcoinj P2PKH transaction using #methodName"(submitMethod, methodName) {
        given: "transaction ingredients, a destination address and an amount to send"
        def ingredients = createIngredients(10.btc)
        // since we're not currently using a change address, RPC calls won't let us
        // spend too much 'absurdly-high-fee' or too little in transaction fees
        // so maybe we should use change addresses in these tests?
        Coin amount = 9.900.btc
        def destAddress = client.getNewAddress()

        when: "we build a transaction"
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, destAddress)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), ingredients.privateKey);

        and: "send via submitMethod [P2P, RPC] and generate a block"
        Transaction sentTx = submitMethod(tx)

        then: "the new address has a balance of amount"
        client.getReceivedByAddress(destAddress) == amount  // Verify destAddress balance

        where: "submitMethod is P2P and then RPC"
        [submitMethod, methodName] << submitMethods
    }
}
