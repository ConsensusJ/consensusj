package org.consensusj.jsonrpc.cli.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class JsonRpcServerConfigEntry {
    private final String bitcoinNetwork;
    private final URI uri;
    private final String username;
    private final String password;

    public JsonRpcServerConfigEntry(
                            @JsonProperty("bitcoin-network") String bitcoinNetwork,
                            @JsonProperty("uri") URI uri,
                            @JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        this.bitcoinNetwork = bitcoinNetwork;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public URI getUri() {
        return uri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBitcoinNetwork() {
        return bitcoinNetwork;
    }
}
