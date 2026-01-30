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
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.script.Script;
import org.consensusj.bitcoin.json.conversion.HexUtil;

/**
 * UTXO Info object from Bitcore/Omni {@code getaddressutxos}.
 */
public class AddressUtxoInfo {
    private final Address address;
    private final Sha256Hash txid;
    private final int outputIndex;
    private final Script script;
    private final Coin satoshis;
    private final int height;
    private final boolean coinbase;

    public AddressUtxoInfo(@JsonProperty("address")     Address address,
                           @JsonProperty("txid")        Sha256Hash txid,
                           @JsonProperty("outputIndex") int outputIndex,
                           @JsonProperty("script")      String script,
                           @JsonProperty("satoshis")    long satoshis,
                           @JsonProperty("height")      int height,
                           @JsonProperty("coinbase")    boolean coinbase) {
        this.address = address;
        this.txid = txid;
        this.outputIndex = outputIndex;
        this.script = Script.parse(HexUtil.hexStringToByteArray(script));
        this.satoshis = Coin.ofSat(satoshis);
        this.height = height;
        this.coinbase = coinbase;
    }

    public Address getAddress() {
        return address;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public Script getScript() {
        return script;
    }

    public Coin getSatoshis() {
        return satoshis;
    }

    public int getHeight() {
        return height;
    }

    public boolean isCoinbase() {
        return coinbase;
    }
}
