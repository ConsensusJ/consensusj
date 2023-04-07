package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * Raw, immutable data for an OP_RETURN transaction output
 */
public class TransactionOutputOpReturn implements TransactionOutputData {
    private final Network network;
    private final byte[] opReturnData;

    public TransactionOutputOpReturn(String networkId, byte[] opReturnData) {
        network = BitcoinNetwork.fromIdString(networkId).orElseThrow(() -> new IllegalArgumentException("bad network id"));
        this.opReturnData = opReturnData;
    }

    @Override
    public String networkId() {
        return network.id();
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
