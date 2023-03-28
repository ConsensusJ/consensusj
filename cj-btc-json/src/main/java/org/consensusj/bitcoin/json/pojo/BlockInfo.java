package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Sha256Hash;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockInfo POJO returned by GetBlockInfo
 */
// "strippedsize" property added (present in Bitcoin 0.13)
public class BlockInfo {
    public enum IncludeTxFlag {
        NO,         // Don't include transactions
        IDONLY,     // Only include the Transaction ID/hash
        YES         // Include transactions (in the format of getrawtransaction RPC)
    }
    public final Sha256Hash hash;
    public final int confirmations;
    public final int size;
    public final int height;
    public final int version;
    public final Sha256Hash merkleroot;
    private final int nTx;
    public final Sha256HashList tx;
    public final Instant time;
    public final long nonce;
    public final String bits;
    public final BigDecimal difficulty;
    public final String chainwork;
    public final Sha256Hash previousblockhash;
    public final Sha256Hash nextblockhash;

    public BlockInfo(@JsonProperty("hash")              Sha256Hash hash,
                     @JsonProperty("confirmations")     int confirmations,
                     @JsonProperty("size")              int size,
                     @JsonProperty("height")            int height,
                     @JsonProperty("version")           int version,
                     @JsonProperty("merkleroot")        Sha256Hash merkleroot,
                     @JsonProperty("nTx")               int nTx,
                     @JsonProperty("tx")                Sha256HashList tx,
                     @JsonProperty("time")              long time,
                     @JsonProperty("nonce")             long nonce,
                     @JsonProperty("bits")              String bits,
                     @JsonProperty("difficulty")        BigDecimal difficulty,
                     @JsonProperty("chainwork")         String chainwork,
                     @JsonProperty("previousblockhash") Sha256Hash previousblockhash,
                     @JsonProperty("nextblockhash")     Sha256Hash nextblockhash) {
        this.hash = hash;
        this.confirmations = confirmations;
        this.size = size;
        this.height = height;
        this.version = version;
        this.merkleroot = merkleroot;
        this.nTx = nTx;
        this.tx = tx;
        this.time = Instant.ofEpochSecond(time);
        this.nonce = nonce;
        this.bits = bits;
        this.difficulty = difficulty;
        this.chainwork = chainwork;
        this.previousblockhash = previousblockhash;
        this.nextblockhash = nextblockhash;
    }

    public static class Sha256HashList extends ArrayList<Sha256Hash> {
        @JsonCreator
        public Sha256HashList(List<Sha256Hash> collection) {
            super(collection);
        }
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        return height;
    }

    public int getVersion() {
        return version;
    }

    public Sha256Hash getMerkleroot() {
        return merkleroot;
    }

    public int getNTx() {
        return nTx;
    }

    public Sha256HashList getTx() {
        return tx;
    }

    public Instant getTime() {
        return time;
    }

    public long getNonce() {
        return nonce;
    }

    public String getBits() {
        return bits;
    }

    public BigDecimal getDifficulty() {
        return difficulty;
    }

    public String getChainwork() {
        return chainwork;
    }

    public Sha256Hash getPreviousblockhash() {
        return previousblockhash;
    }

    public Sha256Hash getNextblockhash() {
        return nextblockhash;
    }
}
