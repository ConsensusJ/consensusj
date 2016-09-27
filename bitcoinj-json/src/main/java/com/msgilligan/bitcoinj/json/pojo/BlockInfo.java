package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockInfo POJO returned by GetBlockInfo
 */
@JsonIgnoreProperties(ignoreUnknown=true)
// "strippedsize" property added (present in Bitcoin 0.13)
public class BlockInfo {
    public final Sha256Hash hash;
    public final int confirmations;
    public final int size;
    public final int height;
    public final int version;
    public final Sha256Hash merkleroot;
    public final Sha256HashList tx;
    public final int time;
    public final int nonce;
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
                     @JsonProperty("tx")                Sha256HashList tx,
                     @JsonProperty("time")              int time,
                     @JsonProperty("nonce")             int nonce,
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
        this.tx = tx;
        this.time = time;
        this.nonce = nonce;
        this.bits = bits;
        this.difficulty = difficulty;
        this.chainwork = chainwork;
        this.previousblockhash = previousblockhash;
        this.nextblockhash = nextblockhash;
    }

    public static class Sha256HashList extends ArrayList<Sha256Hash> {
    }

}
