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

import org.bitcoinj.base.Coin;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * Raw, immutable data for an OP_RETURN transaction output
 */
public class TransactionOutputOpReturn implements TransactionOutputData {
    private final byte[] opReturnData;

    public TransactionOutputOpReturn(byte[] opReturnData) {
        this.opReturnData = opReturnData;
    }

    @Override
    public Coin amount() {
        return Coin.ofSat(0);
    }

    @Override
    public Script scriptPubKey() {
        return new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(opReturnData)
                .build();
    }

    public byte[] opReturnData() {
        return opReturnData;
    }

}
