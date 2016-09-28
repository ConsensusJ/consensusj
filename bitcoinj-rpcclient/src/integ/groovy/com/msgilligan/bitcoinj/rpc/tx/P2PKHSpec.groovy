package com.msgilligan.bitcoinj.rpc.tx

import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.ScriptBuilder

/**
 * Create, send and verify P2PKH transactions via P2P and RPC
 */
class P2PKHSpec extends TxTestBaseSpec {

    def "create and send a bitcoinj P2PKH transaction using P2P"() {
        given: "transaction ingredients, a destination address and an amount to send"
        def ingredients = createIngredients(50.btc)
        Coin amount = 1.btc
        def destAddress = getNewAddress()

        when: "we build a transaction"
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, destAddress)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), ingredients.privateKey);

        and: "send via P2P and generate a block"
        Transaction sentTx = submitP2P(tx)

        then: "the destination address has a balance of amount"
        getReceivedByAddress(destAddress) == amount  // Verify rpcAddress balance
    }

    def "create and send a bitcoinj P2PKH transaction using RPC"() {
        given: "transaction ingredients, a destination address and an amount to send"
        def ingredients = createIngredients(50.btc)
        // since we're not currently using a change address, RPC calls won't let us
        // spend too much 'absurdly-high-fee' or too little in transaction fees
        // so maybe we should use change addresses in these tests?
        Coin amount = 49.999.btc
        def destAddress = getNewAddress()

        when: "we build a transaction"
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, destAddress)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), ingredients.privateKey);

        and: "send via RPC and generate a block"
        Transaction sentTx = submitRPC(tx)

        then: "the new address has a balance of amount"
        getReceivedByAddress(destAddress) == amount  // Verify rpcAddress balance
    }
}
