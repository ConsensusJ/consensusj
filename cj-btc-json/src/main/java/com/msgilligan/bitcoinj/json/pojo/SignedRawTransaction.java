package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
