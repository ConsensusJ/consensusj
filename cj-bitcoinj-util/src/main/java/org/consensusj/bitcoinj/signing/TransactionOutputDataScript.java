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

/**
 *
 */
public class TransactionOutputDataScript implements TransactionOutputData {
    private final Coin amount;
    private final Script script;

    public TransactionOutputDataScript(Coin amount, Script script) {
        this.amount = amount;
        this.script = script;
    }

    @Override
    public Coin amount() {
        return amount;
    }

    @Override
    public Script scriptPubKey() {
        return script;
    }
}
