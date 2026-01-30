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

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.script.Script;
import org.consensusj.bitcoin.json.conversion.HexUtil;

/**
 *
 */
public class PrevTxOutInfo implements UtxoInfo {
    private final Sha256Hash txId;
    private final int vout;
    private final Script scriptPubKey;
    private final String redeemScript;
    private final String witnessScript;
    private final Coin amount;

    public PrevTxOutInfo(Sha256Hash txId, int vout, String scriptPubKey, String redeemScript, String witnessScript, Coin amount) {
        this.txId = txId;
        this.vout = vout;
        this.scriptPubKey = Script.parse(HexUtil.hexStringToByteArray(scriptPubKey));
        this.redeemScript = redeemScript;
        this.witnessScript = witnessScript;
        this.amount = amount;
    }

    @Override
    public Sha256Hash getTxid() {
        return txId;
    }

    @Override
    public int getVout() {
        return vout;
    }

    @Override
    public Script getScriptPubKey() {
        return scriptPubKey;
    }

    @Override
    public String getRedeemScript() {
        return redeemScript;
    }

    @Override
    public String getWitnessScript() {
        return witnessScript;
    }

    @Override
    public Coin getAmount() {
        return amount;
    }
}
