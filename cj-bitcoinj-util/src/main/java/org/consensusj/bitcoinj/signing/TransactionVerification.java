/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
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
        correctlySpendsInput(tx, inputIndex, ScriptBuilder.createOutputScript(fromAddr));
    }

    /**
     * Verify that a transaction correctly spends the input specified by index. Throws {@link ScriptException}
     * if verification fails.
     *
     * @param tx The transaction to verify
     * @param inputIndex The input to verify
     * @param scriptPubKey The script we are trying to spend
     * @throws ScriptException If {@code scriptSig#correctlySpends} fails with exception
     */
    public static void correctlySpendsInput(Transaction tx, int inputIndex, Script scriptPubKey) throws ScriptException {
        TransactionInput input = tx.getInputs().get(inputIndex);
        input.getScriptSig()
                    .correctlySpends(tx, inputIndex, input.getWitness(), input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);
    }
}
