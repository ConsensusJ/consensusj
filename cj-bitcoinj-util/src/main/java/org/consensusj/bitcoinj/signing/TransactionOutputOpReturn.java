package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * Raw, immutable data for an OP_RETURN transaction output
 */
public class TransactionOutputOpReturn implements TransactionOutputData {
    private final byte[] opReturnData;

    public TransactionOutputOpReturn(byte[] opReturnData) {
        this.opReturnData = opReturnData;
    }

    @Override
    public Coin amount() {
        return Coin.ofSat(0);
    }

    @Override
    public Script scriptPubKey() {
        return new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(opReturnData)
                .build();
    }

    public byte[] opReturnData() {
        return opReturnData;
    }

}
