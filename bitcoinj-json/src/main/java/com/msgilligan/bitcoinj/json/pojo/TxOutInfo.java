package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.util.Map;

/**
 * Result of `gettxout`
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
