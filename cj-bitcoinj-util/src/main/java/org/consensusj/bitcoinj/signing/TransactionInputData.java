package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

/**
 *
 */
public interface TransactionInputData {
    Coin amount();
    Script script();

    /**
     * This probably shouldn't be here but is needed for proper operation with bitcoinj
     * @return A Transaction "outpoint" pointing to the output corresponding to this input.
     */
    TransactionOutPoint toOutPoint(Network network);

    /**
     * Create a transaction input from an unspent transaction output.
     * @param out An unspent transaction output
     * @return transaction input data
     */
    static TransactionInputDataImpl fromTxOut(TransactionOutput out) {
        return new TransactionInputDataImpl(out.getParentTransactionHash(), out.getIndex(), out.getValue(), out.getScriptPubKey());
    }
}
