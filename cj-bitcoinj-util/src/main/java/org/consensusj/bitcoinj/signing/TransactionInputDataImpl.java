package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

/**
 * Immutable aggregate of data for TransactionInput.
 * <p>
 * This aspires to be a Java record someday
 */
public class TransactionInputDataImpl implements TransactionInputData {
    private final Sha256Hash txId;
    private final long index;
    private final long amount;
    private final Script script;

    /**
     * @param txId parent txId (shouldn't be needed but Transaction.addSignedInput currently needs an Outpoint)
     * @param index index of unspent output
     * @param amount amount of unspent output
     * @param script
     */
    public TransactionInputDataImpl(Sha256Hash txId, long index, Coin amount, Script script) {
        this.txId = txId;
        this.index = index;
        this.amount = amount != null ? amount.getValue() : 0;        // TODO: Throw when amount is null?
        this.script = script;
    }

    /**
     * @param txId parent txId (shouldn't be needed but Transaction.addSignedInput currently needs an Outpoint)
     * @param index index of unspent output
     * @param amount amount of unspent output
     * @param address
     */
    public TransactionInputDataImpl(Sha256Hash txId, long index, Coin amount, Address address) {
        this(txId, index, amount, ScriptBuilder.createOutputScript(address));
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
    
    public TransactionInput toMutableInput(Network network) {
        return createTransactionInput(toOutPoint(network), Coin.ofSat(amount), script);
    }

    @Override
    public TransactionOutPoint toOutPoint(Network network) {
        return new TransactionOutPoint(NetworkParameters.of(network), index, txId);
    }

    private static TransactionInput createTransactionInput(TransactionOutPoint outPoint, Coin amount, Script script) {
        return new TransactionInput(outPoint.getParams(), null, script.getProgram(), outPoint, amount);
    }
}
