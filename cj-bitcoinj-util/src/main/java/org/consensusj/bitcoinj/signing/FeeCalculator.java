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

/**
 * Interface for transaction fee calculation.
 */
public interface FeeCalculator {
    /**
     * Calculate the fee for an almost-complete transaction. The proposed transaction
     * should contain all inputs and outputs. Typically, this means having a change output
     * with a value of {@link Coin#ZERO}. After calculating the correct fee the change output
     * should be updated with the correct amount.
     *
     * @param proposedTx A nearly-complete proposed transaction.
     * @return A recommended fee for this transaction.
     */
    Coin calculateFee(SigningRequest proposedTx);
}
