package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * Raw, immutable data for an OP_RETURN transaction output
 */
public class TransactionOutputOpReturn implements TransactionOutputData {
    private final NetworkParameters netParams;
    private final byte[] opReturnData;

    public TransactionOutputOpReturn(String networkId, byte[] opReturnData) {
        netParams = NetworkParameters.fromID(networkId);
        if (netParams == null) {
            throw new IllegalArgumentException("bad network id");
        }
        this.opReturnData = opReturnData;
    }

    @Override
    public String networkId() {
        return netParams.getId();
    }

    @Override
    public Coin amount() {
        return Coin.ofSat(0);
    }

    @Override
    public Script script() {
        return new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(opReturnData)
                .build();
    }

    public byte[] opReturnData() {
        return opReturnData;
    }

}
