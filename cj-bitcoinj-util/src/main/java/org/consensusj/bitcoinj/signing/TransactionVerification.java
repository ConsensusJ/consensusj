package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptException;

/**
 * Utility for transaction validation
 */
public class TransactionVerification {
    /**
     * Verify that a transaction correctly spends the input specified by index. Throws {@link ScriptException}
     * if verification fails.
     *
     * @param tx The transaction to verify
     * @param inputIndex The input to verify
     * @param fromAddr The address we are trying to spend funds from
     * @throws ScriptException If {@code scriptSig#correctlySpends} fails with exception
     */
    public static void correctlySpendsInput(Transaction tx, int inputIndex, Address fromAddr) throws ScriptException {
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddr);
        TransactionInput input = tx.getInputs().get(inputIndex);
        if (fromAddr instanceof LegacyAddress) {
            input.getScriptSig()
                    .correctlySpends(tx, inputIndex, null, input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);
        } else {
            input.getScriptSig()
                    .correctlySpends(tx, inputIndex, input.getWitness(), input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);

        }
    }
}
