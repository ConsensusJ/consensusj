package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptException;

import java.util.Optional;

/**
 * Immutable aggregate of data for TransactionInput.
 * <p>
 * This aspires to be a Java record someday
 */
public class TransactionInputDataImpl implements TransactionInputData {
    private final Network network;
    private final Sha256Hash txId;
    private final long index;
    private final long amount;
    private final Script script;

    public TransactionInputDataImpl(String networkId, Sha256Hash txId, long index, Coin amount, Script script) {
        this.network = BitcoinNetwork.fromIdString(networkId).orElseThrow(() -> new IllegalArgumentException("Invalid network ID"));
        this.txId = txId;
        this.index = index;
        this.amount = amount.getValue();
        this.script = script;
    }

    public TransactionInputDataImpl(String networkId, Sha256Hash txId, long index, Coin amount, Address address) {
        this(networkId, txId, index, amount, ScriptBuilder.createOutputScript(address));
    }

    public TransactionInputDataImpl(String networkId, byte[] txId, long index, long satoshis, byte[] scriptBytes) {
        this(networkId, Sha256Hash.wrap(txId), index, Coin.ofSat(satoshis), new Script(scriptBytes));
    }

    public TransactionInputDataImpl(String networkId, Sha256Hash txId, long index, Coin amount, byte[] scriptBytes) {
        this(networkId, txId, index, amount, new Script(scriptBytes));
    }

    public Sha256Hash txId() {
        return txId;
    }

    public long index() {
        return index;
    }

    @Override
    public Coin amount() { return Coin.ofSat(amount); }

    @Override
    public Script script() {
        return script;
    }
    
    public TransactionInput toMutableInput() {
        return createTransactionInput(toOutPoint(), Coin.ofSat(amount), script);
    }

    @Override
    public TransactionOutPoint toOutPoint() {
        return new TransactionOutPoint(NetworkParameters.of(network), index, txId);
    }

    private static TransactionInput createTransactionInput(TransactionOutPoint outPoint, Coin amount, Script script) {
        return new TransactionInput(outPoint.getParams(), null, script.getProgram(), outPoint, amount);
    }
}
