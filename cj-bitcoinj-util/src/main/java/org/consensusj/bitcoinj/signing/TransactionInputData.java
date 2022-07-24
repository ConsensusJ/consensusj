package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

import java.util.Optional;

/**
 *
 */
public interface TransactionInputData {
    String networkId();
    Coin amount();
    Script script();
    Optional<Address> address();

    /**
     * This probably shouldn't be here but is needed for proper operation with bitcoinj
     * @return A Transaction "outpoint" pointing to the output corresponding to this input.
     */
    TransactionOutPoint toOutPoint();
}
