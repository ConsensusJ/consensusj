package com.msgilligan.bitcoinj.rpc.tx

import com.msgilligan.bitcoinj.BaseRegTestSpec
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo
import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import com.msgilligan.bitcoinj.test.RegTestFundingSource
import com.msgilligan.bitcoinj.test.TransactionIngredients
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import spock.lang.Shared

/**
 * Base test class for testing bitcoinj transactions via P2P and RPC on RegTest
 */
abstract class TxTestBaseSpec extends BaseRegTestSpec {
    @Shared
    NetworkParameters params
    @Shared
    PeerGroup peerGroup

    // Array for data-driven testing that varies the submit method
    @Shared
    def submitMethods = [[this.&submitP2P, "P2P"], [this.&submitRPC, "RPC"]]

    void setupSpec() {
        params = getNetParams()
        peerGroup = new PeerGroup(params)
        peerGroup.start()
    }

    TransactionIngredients createIngredients(Coin amount) {
        return ((RegTestFundingSource)fundingSource).createIngredients(amount)
    }

    Transaction submitP2P(Transaction tx) {
        Transaction sentTx = peerGroup.broadcastTransaction(tx).future().get();
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        generate()
        return sentTx
    }

    Transaction submitRPC(Transaction tx) {
        Sha256Hash txid = sendRawTransaction(tx)
        generate()
        Transaction sentTx = getRawTransaction(txid)
        RawTransactionInfo txinfo = getRawTransactionInfo(txid)
        assert txinfo.confirmations == 1
        return sentTx
    }

    /**
     * Wait for a transaction to show up on the server (as unconfirmed)
     * (e.g. to make sure Transaction is fully received before generating a block)
     *
     * @param txid Transaction ID (hash) of transaction we're waiting for
     */
    void waitForUnconfirmedTransaction(Sha256Hash txid) {
        Transaction pendingTx = null;
        while (pendingTx == null) {
            try {
                pendingTx = getRawTransaction(txid)
            } catch (JsonRPCStatusException e) {
                if (e.message != "No information available about transaction") {
                    throw e
                }
                Thread.sleep(250)   // wait 250 milliseconds
            }
        }
    }
}
