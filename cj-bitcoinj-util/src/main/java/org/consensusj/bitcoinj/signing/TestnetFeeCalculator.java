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
import org.bitcoinj.core.Transaction;

/**
 * Stupid simple fee calculator for testnet
 */
public class TestnetFeeCalculator implements FeeCalculator {

    @Override
    public Coin calculateFee(SigningRequest proposedTx) {
        long messageSize =  2048;   // TODO: Size calculation
        long fee = (messageSize * Transaction.DEFAULT_TX_FEE.toSat()) / 1024;
        return Coin.valueOf(fee);
    }
}
