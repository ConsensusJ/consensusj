package com.msgilligan.bitcoinj.test

import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo
import org.consensusj.jsonrpc.JsonRpcStatusException
import org.bitcoinj.core.Coin
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction

import java.util.function.UnaryOperator

/**
 * Test support for testing client-generated bitcoinj transactions
 * in RegTest mode by sending them via P2P and/or RPC
 *
 * TODO: for some reason the tests in P2P mode are intermittently failing on Travis with an infinite loop displaying:
 * `json error code: -5, message: No such mempool or blockchain transaction. Use gettransaction for wallet transactions`
 * So for now we'll only run the RPC methods See the commented out entry in `submitMethods`  below.
 */
trait JTransactionTestSupport implements BTCTestSupport {
    private PeerGroup peerGroup
    private List<SubmitMethod> submitMethods = [
            // Disable P2P temporarily due to intermittent TravisCI failures
            // new SubmitMethod("P2P", this::submitP2P),
            new SubmitMethod("RPC", this::submitRPC)
    ]

    static class SubmitMethod {
        final String name;
        final UnaryOperator<Transaction> method;

        SubmitMethod(String name, UnaryOperator<Transaction> method) {
            this.name = name
            this.method = method
        }
    }

    private void setupPeerGroup() {
        peerGroup = new PeerGroup(client.getNetParams())
        peerGroup.start()
    }

    TransactionIngredients createIngredients(Coin amount) {
        return ((RegTestFundingSource)fundingSource).createIngredients(amount)
    }

    List<SubmitMethod> getSubmitMethods() {
        return submitMethods
    }

    Transaction submitP2P(Transaction tx) {
        if (peerGroup == null) {
            setupPeerGroup()
        }
        Transaction sentTx = peerGroup.broadcastTransaction(tx).future().get();
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.txId)
        client.generateBlocks(1)
        return sentTx
    }

    Transaction submitRPC(Transaction tx) {
        Sha256Hash txid = client.sendRawTransaction(tx)
        client.generateBlocks(1)
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
            } catch (JsonRpcStatusException e) {
                if ((e.message != "No information available about transaction") &&
                    (e.message != "No such mempool or blockchain transaction. Use gettransaction for wallet transactions.")){
                    throw e
                }
                Thread.sleep(250)   // wait 250 milliseconds
            }
        }
    }
}