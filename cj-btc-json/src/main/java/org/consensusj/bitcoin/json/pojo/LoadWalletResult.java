package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for {@code createwallet} and {@code loadwallet}
 */
public class LoadWalletResult {
      private final String name;
      private final String warning;


    @JsonCreator
    public LoadWalletResult(@JsonProperty("name") String name, @JsonProperty("warning") String warning) {
        this.name = name;
        this.warning = warning;
    }

    public String getName() {
        return name;
    }

    public String getWarning() {
        return warning;
    }
}
