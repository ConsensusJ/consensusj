package org.consensusj.bitcoin.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.NetworkParameters;

import java.net.URI;

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

    public RpcConfig(String netIdString,  URI uri, String username, String password) {
        this(NetworkParameters.fromID(netIdString),
                uri,
                username,
                password);
    }


    @JsonCreator
    public RpcConfig(@JsonProperty("netid")     String netIdString,
                     @JsonProperty("uri")       String uri,
                     @JsonProperty("username")  String username,
                     @JsonProperty("password")  String password)  {
        this(NetworkParameters.fromID(netIdString),
                URI.create(uri),
                username,
                password);
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
