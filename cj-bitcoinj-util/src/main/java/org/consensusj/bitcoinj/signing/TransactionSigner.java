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
import org.bitcoinj.wallet.DeterministicKeyChain;

import java.util.concurrent.CompletableFuture;

/**
 * A low-level transaction signing interface that can sign a {@link SigningRequest} that is a complete
 * transaction that only needs signatures. The {@link HDKeychainSigner} implementation can sign transactions
 * using a <b>bitcoinj</b> {@link DeterministicKeyChain}.
 */
public interface TransactionSigner {
    /**
     * Create a signed bitcoinj transaction from the signing request. This is asynchronous because
     * user (or other) confirmation may be required.
     * @param request Signing request with data for all inputs and all outputs
     * @return A signed transaction (should be treated as immutable)
     */
    CompletableFuture<Transaction> signTransaction(SigningRequest request);
}
