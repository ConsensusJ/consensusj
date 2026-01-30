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
package org.consensusj.bitcoinj.service;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.util.concurrent.CompletableFuture;

/**
 * Simple interface for sending signed "raw" transactions
 */
public interface SendTransactionService {

    /**
     * Broadcast a signed transaction on the P2P network
     * @param signedTransaction A signed, ready-to-broadcast transaction
     * @param maxFeeRate Reject transactions whose fee rate is higher than the specified value, expressed in BTC/kB.
     * @return A future for the transaction hash
     */
    CompletableFuture<Sha256Hash> sendRawTransaction(Transaction signedTransaction, Coin maxFeeRate);

    /**
     * Broadcast a signed transaction on the P2P network.
     * <p>
     * This method has no maxFeeRate and behaves as if maxFeeRate were set to zero.
     * @param signedTransaction A signed, ready-to-broadcast transaction
     * @return A future for the transaction hash
     */
    default CompletableFuture<Sha256Hash>  sendRawTransaction(Transaction signedTransaction) {
        return sendRawTransaction(signedTransaction, Coin.ZERO);
    }
}
