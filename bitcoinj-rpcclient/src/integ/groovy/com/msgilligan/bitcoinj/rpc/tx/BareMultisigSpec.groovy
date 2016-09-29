package com.msgilligan.bitcoinj.rpc.tx

import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.Script
import spock.lang.Stepwise
import spock.lang.Unroll

/**
 * Non-P2SH multisig - Fund and redeem a multisig output
 */
@Stepwise
class P2SHSpec extends TxTestBaseSpec {

    private static final ECKey key1 = new ECKey();
    private static final ECKey key2 = new ECKey();
    private static final ECKey key3 = new ECKey();
    private static final List<ECKey> keys = [key1, key2, key3]

    def "create and send a bitcoinj P2SH transaction"() {
        given: "transaction ingredients, a destination address and an amount to send"
        def ingredients = createIngredients(50.btc)
        // since we're not currently using a change address, RPC calls won't let us
        // spend too much 'absurdly-high-fee' or too little in transaction fees
        // so maybe we should use change addresses in these tests?
        Coin amount = 49.999.btc
        //def destAddress = getNewAddress()

        when: "we build a transaction"
        Transaction tx = new Transaction(params)

        // 2-of-3 multisig
        Script script = ScriptBuilder.createMultiSigOutputScript(2, keys)
        tx.addOutput(amount, script)
        // Assume only 1 (first) outpoint is needed (assuming utxos made by createIngredients are big enough)
        tx.addSignedInput(ingredients.outPoints.get(0), ScriptBuilder.createOutputScript(ingredients.address), ingredients.privateKey);

        and: "send via submitMethod [P2P, RPC] and generate a block"
        Transaction sentTx = submitRPC(tx)

        then: "the new address has a balance of amount"
        getReceivedByAddress(destAddress) == amount  // Verify destAddress balance
    }

    def "redeem a multisig transaction"() {

    }

}
