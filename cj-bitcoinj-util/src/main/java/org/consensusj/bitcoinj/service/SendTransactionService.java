package org.consensusj.bitcoinj.service;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.util.concurrent.CompletableFuture;

/**
 * Simple interface for sending signed "raw" transactions
 */
public interface SendTransactionService {

    /**
     * Broadcast a signed transaction on the P2P network
     * @param signedTransaction A signed, ready-to-broadcast transaction
     * @param maxFeeRate Reject transactions whose fee rate is higher than the specified value, expressed in BTC/kB.
     * @return A future for the transaction hash
     */
    CompletableFuture<Sha256Hash> sendRawTransaction(Transaction signedTransaction, Coin maxFeeRate);

    /**
     * Broadcast a signed transaction on the P2P network.
     * <p>
     * This method has no maxFeeRate and behaves as if maxFeeRate were set to zero.
     * @param signedTransaction A signed, ready-to-broadcast transaction
     * @return A future for the transaction hash
     */
    default CompletableFuture<Sha256Hash>  sendRawTransaction(Transaction signedTransaction) {
        return sendRawTransaction(signedTransaction, Coin.ZERO);
    }
}
