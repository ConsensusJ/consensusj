package org.consensusj.bitcoinj.signing.internal;

import org.bitcoinj.base.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionWitness;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptError;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.script.ScriptPattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * Subclass of Transaction that adds a subset of PR #2274
 * See https://github.com/bitcoinj/bitcoinj/pull/2274
 */
public class PatchedTransaction extends Transaction {
    public PatchedTransaction(NetworkParameters params) {
        super(params);
    }

    public TransactionInput addSignedInput(TransactionOutPoint prevOut, Script scriptPubKey, Coin amount, ECKey sigKey,
                                           SigHash sigHash, boolean anyoneCanPay) throws ScriptException {
        // Verify the API user didn't try to do operations out of order.
        checkState(!getOutputs().isEmpty(), "Attempting to sign tx without outputs.");
        TransactionInput input = new TransactionInput(params, this, new byte[] {}, prevOut, amount);
        addInput(input);
        int inputIndex = getInputs().size() - 1;
        if (ScriptPattern.isP2PK(scriptPubKey)) {
            TransactionSignature signature = calculateSignature(inputIndex, sigKey, scriptPubKey, sigHash,
                    anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createInputScript(signature));
            input.setWitness(null);
        } else if (ScriptPattern.isP2PKH(scriptPubKey)) {
            TransactionSignature signature = calculateSignature(inputIndex, sigKey, scriptPubKey, sigHash,
                    anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createInputScript(signature, sigKey));
            input.setWitness(null);
        } else if (ScriptPattern.isP2WPKH(scriptPubKey)) {
            Script scriptCode = ScriptBuilder.createP2PKHOutputScript(sigKey);
            TransactionSignature signature = calculateWitnessSignature(inputIndex, sigKey, scriptCode, input.getValue(),
                    sigHash, anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createEmpty());
            input.setWitness(TransactionWitness.redeemP2WPKH(signature, sigKey));
        } else {
            throw new ScriptException(ScriptError.SCRIPT_ERR_UNKNOWN_ERROR, "Don't know how to sign for this kind of scriptPubKey: " + scriptPubKey);
        }
        return input;
    }

}
