/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
