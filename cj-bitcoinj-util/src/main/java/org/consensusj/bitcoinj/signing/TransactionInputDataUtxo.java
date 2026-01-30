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
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

import java.util.Objects;

/**
 * Immutable aggregate of data for TransactionInput. This holds the same data as {@link Utxo.Complete} and
 * the two may be combined in the future.
 */
public class TransactionInputDataUtxo implements TransactionInputData, Utxo {
    private final Sha256Hash txId;
    private final int index;
    private final long amount;
    private final Script script;

    /**
     * @param txId parent txId (shouldn't be needed but Transaction.addSignedInput currently needs an Outpoint)
     * @param index index of unspent output
     * @param amount amount of unspent output
     * @param script This is the scriptPubKey of the utxo we want to spend
     */
    public TransactionInputDataUtxo(Sha256Hash txId, int index, Coin amount, Script script) {
        this.txId = Objects.requireNonNull(txId);
        this.index = index;
        this.amount = amount != null ? amount.getValue() : 0;        // TODO: Throw when amount is null?
        this.script = Objects.requireNonNull(script);
    }

    public Sha256Hash txId() {
        return txId;
    }

    public int index() {
        return index;
    }

    @Override
    public Coin amount() { return Coin.ofSat(amount); }

    @Override
    public Script script() {
        return script;
    }
    
    public TransactionInput toMutableInput() {
        return createTransactionInput(toOutPoint(), Coin.ofSat(amount), script);
    }

    @Override
    public Utxo.Complete toUtxo() {
        return Utxo.of(txId, index, Coin.ofSat(amount), script);
    }

    @Override
    public TransactionOutPoint toOutPoint() {
        return new TransactionOutPoint(index, txId);
    }

    private static TransactionInput createTransactionInput(TransactionOutPoint outPoint, Coin amount, Script script) {
        return new TransactionInput( null, script.program(), outPoint, amount);
    }
}
