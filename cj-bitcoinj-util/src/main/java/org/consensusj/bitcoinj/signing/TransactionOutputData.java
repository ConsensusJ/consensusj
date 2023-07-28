package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

/**
 * Raw, immutable data for a transaction output
 */
public interface TransactionOutputData {
    Coin amount();
    Script scriptPubKey();

    static TransactionOutputData of(Address address, Coin amount) {
        return new TransactionOutputAddress(amount, address);
    }

    static TransactionOutputData fromTxOutput(TransactionOutput out) {
        return new TransactionOutputDataScript(
                out.getValue(),
                out.getScriptPubKey());
    }

    default TransactionOutput toMutableOutput(Network network) {
        return new TransactionOutput(null, amount(), scriptPubKey().getProgram());
    }
}
