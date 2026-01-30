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
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.util.Map;

/**
 * Result of `gettxout`
 */
public class TxOutInfo {
    private final Sha256Hash bestblock;
    private final int        confirmations;
    private final Coin       value;
    private final Map        scriptPubKey;
    private final int        version;
    private final boolean    coinbase;

    @JsonCreator
    public TxOutInfo(@JsonProperty("bestblock")     Sha256Hash  bestblock,
                     @JsonProperty("confirmations") int         confirmations,
                     @JsonProperty("value")         Coin        value,
                     @JsonProperty("scriptPubKey")  Map         scriptPubKey,
                     @JsonProperty("version")       int         version,
                     @JsonProperty("coinbase")      boolean     coinbase) {
        this.bestblock = bestblock;
        this.confirmations = confirmations;
        this.value = value;
        this.scriptPubKey = scriptPubKey;
        this.version = version;
        this.coinbase = coinbase;
    }

    public Sha256Hash getBestblock() {
        return bestblock;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Coin getValue() {
        return value;
    }

    public Map getScriptPubKey() {
        return scriptPubKey;
    }

    public int getVersion() {
        return version;
    }

    public boolean isCoinbase() {
        return coinbase;
    }
}
