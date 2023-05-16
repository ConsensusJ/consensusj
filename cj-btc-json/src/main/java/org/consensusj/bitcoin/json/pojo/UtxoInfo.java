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
