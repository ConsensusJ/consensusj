package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

/**
 * Data class for UnspentOutput as returned by listUnspent RPC
 * Because the class is immutable we have to give Jackson some hints via annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnspentOutput {
    private final Sha256Hash  txid;
    private final int         vout;
    private final Address     address;
    private final String      label;
    private final String      scriptPubKey;
    private final Coin        amount;
    private final int         confirmations;


    private final String      redeemScript;
    private final String      witnessScript;
    private final boolean     spendable;
    private final boolean     solvable;
    private final String      desc;
    private final boolean     safe;

    @JsonCreator
    public UnspentOutput(@JsonProperty("txid")          Sha256Hash  txid,
                         @JsonProperty("vout")          int         vout,
                         @JsonProperty("address")       Address     address,
                         @JsonProperty("label")         String      label,
                         @JsonProperty("scriptPubKey")  String      scriptPubKey,
                         @JsonProperty("amount")        Coin        amount,
                         @JsonProperty("confirmations") int         confirmations,
                         @JsonProperty("redeemScript")  String      redeemScript,
                         @JsonProperty("witnessScript") String      witnessScript,
                         @JsonProperty("spendable")     boolean     spendable,
                         @JsonProperty("solvable")      boolean     solvable,
                         @JsonProperty("desc")          String      desc,
                         @JsonProperty("safe")          boolean     safe) {
        this.txid = txid;
        this.vout = vout;
        this.address = address;
        this.label = label;
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        this.confirmations = confirmations;
        this.redeemScript = redeemScript;
        this.witnessScript = witnessScript;
        this.spendable = spendable;
        this.solvable = solvable;
        this.desc = desc;
        this.safe = safe;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getVout() {
        return vout;
    }

    public Address getAddress() {
        return address;
    }

    public String getLabel() {
        return label;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public Coin getAmount() {
        return amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public String getRedeemScript() {
        return redeemScript;
    }

    public String getWitnessScript() {
        return witnessScript;
    }

    public boolean isSpendable() {
        return spendable;
    }

    public boolean isSolvable() {
        return solvable;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isSafe() {
        return safe;
    }
}
