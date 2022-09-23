package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for {@code createwallet} and {@code loadwallet}
 */
public class UnloadWalletResult {
    private final String warning;
    
    @JsonCreator
    public UnloadWalletResult(@JsonProperty("warning") String warning) {
        this.warning = warning;
    }
    
    public String getWarning() {
        return warning;
    }
}
