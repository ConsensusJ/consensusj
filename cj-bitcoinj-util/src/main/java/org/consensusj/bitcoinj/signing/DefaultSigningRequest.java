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
                utx.addInput(in.toOutPoint().hash(),
                        in.toOutPoint().index(),
                        ScriptBuilder.createEmpty()));
        this.outputs().forEach(out ->
                utx.addOutput(new TransactionOutput(utx,
                        out.amount(),
                        out.scriptPubKey().program())));
        return utx;
    }
}
