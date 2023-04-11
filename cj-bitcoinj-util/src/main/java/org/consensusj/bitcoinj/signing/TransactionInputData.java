package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.TransactionOutPoint;
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
}
