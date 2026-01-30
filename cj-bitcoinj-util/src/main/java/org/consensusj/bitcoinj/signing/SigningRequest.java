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
import org.bitcoinj.core.Transaction;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// A SigningRequest should be immutable *and* complete/ready-to-sign. There should be some kind of SigningRequestBuilder
// to build a SigningRequest.
/**
 * A transaction signing request with immutable data specifying the transaction.
 * <p>
 * This is an experiment as to what immutable transactions might look like in <b>bitcoinj</b>.
 * At some point in the future I would like to propose some refactoring in bitcoinj to implement
 * immutable transactions in a mostly-compatible way with the existing transaction classes.
 */
public interface SigningRequest {
    List<TransactionInputData> inputs();
    List<TransactionOutputData> outputs();

    static SigningRequest of(List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        return new DefaultSigningRequest(inputs, outputs);
    }

    static SigningRequest of(List<TransactionInputData> inputs, Map<Address, Coin> outputMap) {
        List<TransactionOutputData> outs = outputMap.entrySet().stream()
                .map(e -> new TransactionOutputAddress(e.getValue(), e.getKey()))
                .collect(Collectors.toList());
        return SigningRequest.of(inputs, outs);
    }

    /**
     * bitcoinj signing (currently) uses mutable {@link Transaction} objects, this convenience method will create
     * a completed, unsigned bitcoinj {@code Transaction} if you want to use bitcoinj to sign this request.
     * @return an unsigned bitcoinj transaction
     */
    Transaction toUnsignedTransaction();
}
