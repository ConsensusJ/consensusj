package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class SignedRawTransaction {
    private final String hex;
    private final boolean complete;

    @JsonCreator
    public SignedRawTransaction(@JsonProperty("hex")        String   hex,
                                @JsonProperty("complete")   boolean  complete) {
        this.hex = hex;
        this.complete = complete;
    }

    public  String getHex() {
        return hex;
    }

    public boolean isComplete() {
        return complete;
    }
}
