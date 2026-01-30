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
package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.util.List;

/**
 *
 */
public class ReceivedByAddressInfo {
    public final Address address;
    public final Coin amount;
    public final int confirmations;
    public final List<Sha256Hash> txids;
    public final String label;

    @JsonCreator
    public ReceivedByAddressInfo(@JsonProperty("address") Address address,
                                 @JsonProperty("amount") Coin amount,
                                 @JsonProperty("confirmations") int confirmations,
                                 @JsonProperty("txids") List<Sha256Hash> txids,
                                 @JsonProperty("label") String label) {
        this.address = address;
        this.amount = amount;
        this.confirmations = confirmations;
        this.txids = txids;
        this.label = label;
    }

    public Address getAddress() {
        return address;
    }

    public Coin getAmount() {
        return amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public List<Sha256Hash> getTxids() {
        return txids;
    }
}
