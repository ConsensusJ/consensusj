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
