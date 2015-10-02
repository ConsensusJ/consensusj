package com.msgilligan.bitcoinj.rpc;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import sun.nio.ch.Net;

import java.net.URI;

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
