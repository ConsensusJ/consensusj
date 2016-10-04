package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

/**
 * Data class for UnspentOutput as returned by listUnspent RPC
 * Because the class is immutable we have to give Jackson some hints via annotations.
 */
public class UnspentOutput {
    private final Sha256Hash  txid;
    private final int         vout;
    private final Address     address;
    private final String      account;
    private final String      scriptPubKey;
    private final Coin        amount;
    private final int         confirmations;
    private final boolean     spendable;
    private final Boolean     solvable;

    @JsonCreator
    public UnspentOutput(@JsonProperty("txid")          Sha256Hash  txid,
                         @JsonProperty("vout")          int         vout,
                         @JsonProperty("address")       Address     address,
                         @JsonProperty("account")       String      account,
                         @JsonProperty("scriptPubKey")  String      scriptPubKey,
                         @JsonProperty("amount")        Coin        amount,
                         @JsonProperty("confirmations") int         confirmations,
                         @JsonProperty("spendable")     boolean     spendable,
                         @JsonProperty("solvable")      Boolean     solvable) {
        this.txid = txid;
        this.vout = vout;
        this.address = address;
        this.account = account;
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        this.confirmations = confirmations;
        this.spendable = spendable;
        this.solvable = solvable;
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

    public String getAccount() {
        return account;
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

    public boolean getSpendable() { return spendable; }
}
