package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Sha256Hash;

/**
 *
 */
public class ChainTip {
    private final int height;
    private final Sha256Hash hash;
    private final int branchlen;
    private final String status;

    @JsonCreator
    public ChainTip(@JsonProperty("height")     int height,
                    @JsonProperty("hash")       Sha256Hash hash,
                    @JsonProperty("branchlen")  int branchlen,
                    @JsonProperty("status")     String status) {
        this.height = height;
        this.hash = hash;
        this.branchlen = branchlen;
        this.status = status;
    }

    public int getHeight() {
        return height;
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public int getBranchlen() {
        return branchlen;
    }

    public String getStatus() {
        return status;
    }
}
