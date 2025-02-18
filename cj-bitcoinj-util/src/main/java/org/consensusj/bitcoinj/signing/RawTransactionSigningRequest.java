package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents the data in {@code signrawtransactionwithwallet}. Specifically
 * inputs with UTXO hash and index and an empty scriptSig.
 */
public class RawTransactionSigningRequest {
    private final List<RawInput> inputs;
    private final List<TransactionOutputData> outputs;


    public static RawTransactionSigningRequest of(List<RawInput> inputs, List<TransactionOutputData> outputs) {
        return new RawTransactionSigningRequest(inputs, outputs);
    }

    public static RawTransactionSigningRequest ofTransaction(Transaction transaction) {
        List<RawInput> inputs = transaction.getInputs().stream()
                .map(i -> new RawInput(i.getOutpoint().hash(), (int) i.getOutpoint().index(), i.getScriptSig()))
                .collect(Collectors.toList());
        List<TransactionOutputData> outputs = transaction.getOutputs().stream()
                .map(TransactionOutputData::fromTxOutput)
                .collect(Collectors.toList());
        return RawTransactionSigningRequest.of(inputs, outputs);
    }

    public RawTransactionSigningRequest(List<RawInput> inputs, List<TransactionOutputData> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<RawInput> inputs() {
        return inputs;
    }

    public List<TransactionOutputData> outputs() {
        return outputs;
    }

    public static class RawInput {
        private final Sha256Hash txId;
        private final int index;
        private final Script scriptSig;

        /**
         * @param txId id
         * @param index index
         * @param scriptSig typically is empty script (at least in {@code signrawtransactionwithwallet} case
         */
        public RawInput(Sha256Hash txId, int index, Script scriptSig) {
            this.txId = txId;
            this.index = index;
            this.scriptSig = scriptSig;
        }

        public Sha256Hash txId() {
            return txId;
        }

        public int index() {
            return index;
        }

        public Script scriptSig() {
            return scriptSig;
        }

        public Utxo.Basic toUtxo() {
            return Utxo.of(txId, index);
        }
    }
}
