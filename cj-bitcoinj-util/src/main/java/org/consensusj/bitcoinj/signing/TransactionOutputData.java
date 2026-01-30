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
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

/**
 * Raw, immutable data for a transaction output
 */
public interface TransactionOutputData {
    Coin amount();
    Script scriptPubKey();

    static TransactionOutputData of(Address address, Coin amount) {
        return new TransactionOutputAddress(amount, address);
    }

    static TransactionOutputData fromTxOutput(TransactionOutput out) {
        return new TransactionOutputDataScript(
                out.getValue(),
                out.getScriptPubKey());
    }

    default TransactionOutput toMutableOutput() {
        return new TransactionOutput(null, amount(), scriptPubKey().program());
    }
}
