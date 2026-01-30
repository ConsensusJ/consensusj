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
package org.consensusj.bitcoin.json.pojo.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Coin;

/**
 * Result of OmniCore/BitCore {@code getaddressbalance} method.
 */
public class AddressBalanceInfo {
    private final long balance;
    private final long received;
    private final long immature;

    public AddressBalanceInfo(@JsonProperty("balance") long balance,
                              @JsonProperty("received") long received,
                              @JsonProperty("immature") long immature) {
        this.balance = balance;
        this.received = received;
        this.immature = immature;
    }

    /**
     * @return the current balance in satoshis
     */
    public Coin getBalance() {
        return Coin.ofSat(balance);
    }

    /**
     * @return the total number of satoshis received (including change)
     */
    public Coin getReceived() {
        return Coin.ofSat(received);
    }

    /**
     * @return the total number of non-spendable mining satoshis received
     */
    public Coin getImmature() {
        return Coin.ofSat(immature);
    }
}
