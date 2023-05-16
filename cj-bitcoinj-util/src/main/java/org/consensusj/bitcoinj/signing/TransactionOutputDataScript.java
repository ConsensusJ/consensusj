package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.script.Script;

/**
 *
 */
public class TransactionOutputDataScript implements TransactionOutputData {
    private final Coin amount;
    private final Script script;

    public TransactionOutputDataScript(Coin amount, Script script) {
        this.amount = amount;
        this.script = script;
    }

    @Override
    public Coin amount() {
        return amount;
    }

    @Override
    public Script scriptPubKey() {
        return script;
    }
}
