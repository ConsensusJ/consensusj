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
import org.bitcoinj.base.Sha256Hash;

import java.util.List;

/**
 * Result from Bitcore/Omni {@code getaddressutxos}.
 */
public class AddressUtxoResult {
    private final Sha256Hash hash;
    private final int height;
    private final List<AddressUtxoInfo> utxos;

    public AddressUtxoResult(@JsonProperty("hash")    Sha256Hash hash,
                             @JsonProperty("height")  int height,
                             @JsonProperty("utxos")   List<AddressUtxoInfo> utxos) {
        this.hash = hash;
        this.height = height;
        this.utxos = utxos;
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public int getHeight() {
        return height;
    }

    public List<AddressUtxoInfo> getUtxos() {
        return utxos;
    }
}
