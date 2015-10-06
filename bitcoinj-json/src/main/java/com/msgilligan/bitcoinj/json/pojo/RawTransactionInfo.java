package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * RawTransaction POJO
 */
public class RawTransactionInfo {
    public final String hex;
    public final Sha256Hash txid;
    public final int version;
    public final int locktime;
    public final VinList vin;
    public final VoutList vout;
    public final Sha256Hash blockhash;
    public final int confirmations;
    public final int time;
    public final int blocktime;

    public RawTransactionInfo(@JsonProperty("hex")              String hex,
                              @JsonProperty("txid")             Sha256Hash txid,
                              @JsonProperty("version")          int version,
                              @JsonProperty("locktime")         int locktime,
                              @JsonProperty("vin")              VinList vin,
                              @JsonProperty("vout")             VoutList vout,
                              @JsonProperty("blockhash")        Sha256Hash blockhash,
                              @JsonProperty("confirmations")    int confirmations,
                              @JsonProperty("time")             int time,
                              @JsonProperty("blocktime")        int blocktime) {
        this.hex = hex;
        this.txid = txid;
        this.version = version;
        this.locktime = locktime;
        this.vin = vin;
        this.vout = vout;
        this.blockhash = blockhash;
        this.confirmations = confirmations;
        this.time = time;
        this.blocktime = blocktime;
    }

    public String getHex() {
        return hex;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getVersion() {
        return version;
    }

    public int getLocktime() {
        return locktime;
    }

    public List<Vin> getVin() {
        return vin;
    }

    public List<Vout> getVout() {
        return vout;
    }

    public Sha256Hash getBlockhash() {
        return blockhash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public int getTime() {
        return time;
    }

    public int getBlocktime() {
        return blocktime;
    }

    public static class VinList extends ArrayList<Vin> {
    }

    public static class VoutList extends ArrayList<Vout> {
    }

    public static class Vin {
        public final Sha256Hash txid;
        public final  int vout;
        public final  Object scriptSig;
        public final  long sequence;

        public Vin(@JsonProperty("txid") Sha256Hash txid,
                   @JsonProperty("vout") int vout,
                   @JsonProperty("scriptSig") Object scriptSig,
                   @JsonProperty("sequence") long sequence) {
            this.txid = txid;
            this.vout = vout;
            this.scriptSig = scriptSig;
            this.sequence = sequence;
        }

        public Sha256Hash getTxid() {
            return txid;
        }

        public int getVout() {
            return vout;
        }

        public Object getScriptSig() {
            return scriptSig;
        }

        public long getSequence() {
            return sequence;
        }
    }

    public static class Vout {
        public final  Coin value;
        public final  int n;
        public final  Object scriptPubKey;

        public Vout(@JsonProperty("value")          Coin value,
                    @JsonProperty("n")              int n,
                    @JsonProperty("scriptPubKey")   Object scriptPubKey) {
            this.value = value;
            this.n = n;
            this.scriptPubKey = scriptPubKey;
        }

        public Coin getValue() {
            return value;
        }

        public int getN() {
            return n;
        }

        public Object getScriptPubKey() {
            return scriptPubKey;
        }
    }
}
