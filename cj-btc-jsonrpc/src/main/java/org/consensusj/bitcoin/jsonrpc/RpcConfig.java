package org.consensusj.bitcoin.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;

import java.net.URI;

/**
 * Configuration class for JSON-RPC client. Contains complete URL, username, and password.
 */
public class RpcConfig {

    private final Network network;
    private final URI uri;
    private final String   username;
    private final String   password;

    public RpcConfig(Network network, URI uri, String username, String password) {
        this.network = network;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public RpcConfig(String netIdString,  URI uri, String username, String password) {
        this(BitcoinNetwork.fromIdString(netIdString).orElseThrow(() -> new IllegalArgumentException("invalid network string")),
                uri,
                username,
                password);
    }

    @JsonCreator
    public RpcConfig(@JsonProperty("netid")     String netIdString,
                     @JsonProperty("uri")       String uri,
                     @JsonProperty("username")  String username,
                     @JsonProperty("password")  String password)  {
        this(BitcoinNetwork.fromIdString(netIdString).orElseThrow(() -> new IllegalArgumentException("invalid network string")),
                URI.create(uri),
                username,
                password);
    }

    @JsonIgnore
    public Network network() {
        return network;
    }

    @JsonProperty("netid")
    public String getNetIdString() {
        return network.id();
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
