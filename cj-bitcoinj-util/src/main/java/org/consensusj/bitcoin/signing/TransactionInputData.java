package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;

import java.util.Optional;

/**
 * Immutable aggregate of data for TransactionInput.
 * <p>
 * This aspires to be a Java record someday
 */
public class TransactionInputData {
    private final NetworkParameters netParams;
    private final Sha256Hash txId;
    private final long index;
    private final Script script;

    public TransactionInputData(String networkId, Sha256Hash txId, long index, Script script) {
        netParams = NetworkParameters.fromID(networkId);
        if (netParams == null) {
            throw new IllegalArgumentException("Invalid network ID");
        }
        this.txId = txId;
        this.index = index;
        this.script = script;
    }

    public TransactionInputData(String networkId, byte[] txId, long index, byte[] scriptBytes) {
        this(networkId, Sha256Hash.of(txId), index, new Script(scriptBytes));
    }

    public String networkId() {
        return netParams.getId();
    }

    public Sha256Hash txId() {
        return txId;
    }

    public long index() {
        return index;
    }

    public Script script() {
        return script;
    }

    /**
     * @return An address, if available
     */
    public Optional<Address> address() {
        Optional<Address> optAddress;
        try {
            optAddress = Optional.of(script.getToAddress(netParams));
        } catch (ScriptException e ) {
            optAddress = Optional.empty();
        }
        return optAddress;
    }

    public TransactionInput toMutableInput() {
        return createTransactionInput(toOutPoint(), script);
    }

    public TransactionOutPoint toOutPoint() {
        return new TransactionOutPoint(netParams, index, txId);
    }

    private static TransactionInput createTransactionInput(TransactionOutPoint outPoint, Script script) {
        return new TransactionInput(outPoint.getParams(), null, script.getProgram(), outPoint);
    }
}
