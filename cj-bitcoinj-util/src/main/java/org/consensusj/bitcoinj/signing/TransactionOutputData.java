package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.BitcoinNetworkParams;
import org.bitcoinj.script.Script;

/**
 * Raw, immutable data for a transaction output
 */
public interface TransactionOutputData {
    Coin amount();
    Script script();

    default TransactionOutput toMutableOutput(Network network) {
        return new TransactionOutput(BitcoinNetworkParams.of(network), null, amount(), script().getProgram());
    }
}
