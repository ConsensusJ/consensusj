package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed information about an in-wallet transaction (from gettransaction RPC)
 */
public class WalletTransactionInfo {
    private final Coin amount;
    private final Coin fee;
    private final int confirmations;
    private final Sha256Hash blockhash;
    private final int blockindex;
    private final int blocktime;
    private final Sha256Hash txid;
    private final WalletConflictList walletconflicts;
    private final int time;
    private final int timereceived;
    private final String bip125Replaceable;
    private final DetailList details;
    private final String hex;

    @JsonCreator
    public WalletTransactionInfo(@JsonProperty("amount")            Coin amount,
                                 @JsonProperty("fee")               Coin fee,
                                 @JsonProperty("confirmations")     int confirmations,
                                 @JsonProperty("blockhash")         Sha256Hash blockhash,
                                 @JsonProperty("blockindex")        int blockindex,
                                 @JsonProperty("blocktime")         int blocktime,
                                 @JsonProperty("txid")              Sha256Hash txid,
                                 @JsonProperty("walletconflicts")   WalletConflictList walletconflicts,
                                 @JsonProperty("time")              int time,
                                 @JsonProperty("timereceived")      int timereceived,
                                 @JsonProperty("bip125-replaceable") String bip125Replaceable,
                                 @JsonProperty("details")           DetailList details,
                                 @JsonProperty("hex")               String hex) {
        this.amount = amount;
        this.fee = fee;
        this.confirmations = confirmations;
        this.blockhash = blockhash;
        this.blockindex = blockindex;
        this.blocktime = blocktime;
        this.txid = txid;
        this.walletconflicts = walletconflicts;
        this.time = time;
        this.timereceived = timereceived;
        this.bip125Replaceable = bip125Replaceable;
        this.details = details;
        this.hex = hex;
    }

    public Coin getAmount() {
        return amount;
    }

    public Coin getFee() {
        return fee;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Sha256Hash getBlockhash() {
        return blockhash;
    }

    public int getBlockindex() {
        return blockindex;
    }

    public int getBlocktime() {
        return blocktime;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public List<Sha256Hash> getWalletconflicts() {
        return walletconflicts;
    }

    public int getTime() {
        return time;
    }

    public int getTimereceived() {
        return timereceived;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public String getHex() {
        return hex;
    }

    public static class WalletConflictList extends ArrayList<Sha256Hash> {
    }

    public static class DetailList extends ArrayList<Detail> {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Detail {
        private final String account;
        private final Address address;
        private final String category;
        private final Coin amount;
        private final String label;
        private final int vout;
        private final Coin fee;

        public Detail(@JsonProperty("account")  String account,
                      @JsonProperty("address")  Address address,
                      @JsonProperty("category") String category,
                      @JsonProperty("amount")   Coin amount,
                      @JsonProperty("label")    String label,
                      @JsonProperty("vout")     int vout,
                      @JsonProperty("fee")      Coin fee) {
            this.account = account;
            this.address = address;
            this.category = category;
            this.amount = amount;
            this.label = label;
            this.vout = vout;
            this.fee = fee;
        }

        public String getAccount() {
            return account;
        }

        public Address getAddress() {
            return address;
        }

        public String getCategory() {
            return category;
        }

        public Coin getAmount() {
            return amount;
        }

        public int getVout() {
            return vout;
        }

        public Coin getFee() {
            return fee;
        }
    }
}
