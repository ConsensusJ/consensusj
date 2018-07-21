package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class RpcConfig {

    private final NetworkParameters netParams;
    private final URI uri;
    private final String   username;
    private final String   password;

    public RpcConfig(NetworkParameters netParams, URI uri, String username, String password) {
        this.netParams = netParams;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    @JsonCreator
    public RpcConfig(@JsonProperty("netid")     String netIdString,
                     @JsonProperty("uri")       String uri,
                     @JsonProperty("username")  String username,
                     @JsonProperty("password")  String password) throws URISyntaxException {
        this(NetworkParameters.fromID(netIdString),
                new URI(uri),
                username,
                password);
    }

    @Deprecated
    public RpcConfig(URI uri, String username, String password) {
        this(RegTestParams.get(), uri, username, password);
    }

    @JsonIgnore
    public NetworkParameters getNetParams() {
        return netParams;
    }

    @JsonProperty("netid")
    public String getNetIdString() {
        return netParams.getId();
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
