package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.Coin;

/**
 * Interface for transaction fee calculation.
 */
public interface FeeCalculator {
    /**
     * Calculate the fee for an almost-complete transaction. The proposed transaction
     * should contain all inputs and outputs. Typically, this means having a change output
     * with a value of {@link Coin#ZERO}. After calculating the correct fee the change output
     * should be updated with the correct amount.
     *
     * @param proposedTx A nearly-complete proposed transaction.
     * @return A recommended fee for this transaction.
     */
    Coin calculateFee(SigningRequest proposedTx);
}
