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

/**
 * Interface for commonly-used UTXO information, for example the {@code prevtxs} parameter of
 * the {@code signrawtransactionwithwallet} JSON-RPC request.
 */
public interface UtxoInfo {
    Sha256Hash getTxid();

    int getVout();

    Script getScriptPubKey();

    String getRedeemScript();

    String getWitnessScript();

    Coin getAmount();
}
