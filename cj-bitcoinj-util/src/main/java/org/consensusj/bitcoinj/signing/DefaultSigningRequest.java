package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.ScriptBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of SigningRequest
 */
public class DefaultSigningRequest implements SigningRequest {
    private final List<TransactionInputData> inputs;
    private final List<TransactionOutputData> outputs;

    public DefaultSigningRequest(List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
    }

    @Override
    public List<TransactionInputData> inputs() {
        return inputs;
    }

    @Override
    public List<TransactionOutputData> outputs() {
        return outputs;
    }

    /**
     * bitcoinj signing uses (currently) mutable transaction objects, this
     * convenience method will create one if you want to use bitcoinj to sign this request.
     * @return an unsigned bitcoinj transaction
     */
    public Transaction toUnsignedTransaction() {
        Transaction utx = new Transaction();
        this.inputs().forEach(in ->
                utx.addInput(in.toOutPoint().getHash(),
                        in.toOutPoint().getIndex(),
                        ScriptBuilder.createEmpty()));
        this.outputs().forEach(out ->
                utx.addOutput(new TransactionOutput(utx,
                        out.amount(),
                        out.scriptPubKey().getProgram())));
        return utx;
    }
}
