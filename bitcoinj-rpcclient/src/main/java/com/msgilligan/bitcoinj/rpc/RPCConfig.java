package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration class for JSON-RPC client
 *
 * Contains complete URL, username, and password.
 *
 */
public class RPCConfig {

    private final NetworkParameters netParams;
    private final URI uri;
    private final String   username;
    private final String   password;

    public RPCConfig(NetworkParameters netParams, URI uri, String username, String password) {
        this.netParams = netParams;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    @JsonCreator
    public RPCConfig(@JsonProperty("netid")     String netIdString,
                     @JsonProperty("uri")       String uri,
                     @JsonProperty("username")  String username,
                     @JsonProperty("password")  String password) throws URISyntaxException {
        this(NetworkParameters.fromID(netIdString),
                new URI(uri),
                username,
                password);
    }

    @Deprecated
    public RPCConfig(URI uri, String username, String password) {
        this(RegTestParams.get(), uri, username, password);
    }

    public NetworkParameters getNetParams() {
        return netParams;
    }

    public URI getURI() {
        return uri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
