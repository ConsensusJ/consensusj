package org.consensusj.bitcoin.json.pojo;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

/**
 *
 */
public class PrevTxOutInfo implements UtxoInfo {
    private final Sha256Hash txId;
    private final int vout;
    private final String scriptPubKey;
    private final String redeemScript;
    private final String witnessScript;
    private final Coin amount;

    public PrevTxOutInfo(Sha256Hash txId, int vout, String scriptPubKey, String redeemScript, String witnessScript, Coin amount) {
        this.txId = txId;
        this.vout = vout;
        this.scriptPubKey = scriptPubKey;
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
    public String getScriptPubKey() {
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
