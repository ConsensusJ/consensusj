package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo
import com.msgilligan.jsonrpc.JsonRPCStatusException
import org.bitcoinj.core.Coin
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction

/**
 * Test support for testing client-generated bitcoinj transactions
 * in RegTest mode by sending them via P2P and/or RPC
 */
trait JTransactionTestSupport implements BTCTestSupport {
    private PeerGroup peerGroup
    private def submitMethods = [[this.&submitP2P, "P2P"], [this.&submitRPC, "RPC"]]

    private void setupPeerGroup() {
        peerGroup = new PeerGroup(client.getNetParams())
        peerGroup.start()
    }

    TransactionIngredients createIngredients(Coin amount) {
        return ((RegTestFundingSource)fundingSource).createIngredients(amount)
    }

    def getSubmitMethods() {
        return submitMethods
    }

    Transaction submitP2P(Transaction tx) {
        if (peerGroup == null) {
            setupPeerGroup()
        }
        Transaction sentTx = peerGroup.broadcastTransaction(tx).future().get();
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        client.generate()
        return sentTx
    }

    Transaction submitRPC(Transaction tx) {
        Sha256Hash txid = client.sendRawTransaction(tx)
        client.generate()
        Transaction sentTx = client.getRawTransaction(txid)
        RawTransactionInfo txinfo = client.getRawTransactionInfo(txid)
        assert txinfo.confirmations == 1
        return sentTx
    }

    /**
     * Wait for a transaction to show up on the server (as unconfirmed)
     * (e.g. to make sure Transaction is fully received before generating a block)
     *
     * @param txid Transaction ID (hash) of transaction we're waiting for
     */
    private void waitForUnconfirmedTransaction(Sha256Hash txid) {
        Transaction pendingTx = null;
        while (pendingTx == null) {
            try {
                pendingTx = client.getRawTransaction(txid)
            } catch (JsonRPCStatusException e) {
                if (e.message != "No information available about transaction") {
                    throw e
                }
                Thread.sleep(250)   // wait 250 milliseconds
            }
        }
    }

}