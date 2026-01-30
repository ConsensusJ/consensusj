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
package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for `getnetworkinfo` RPC response.
 * Warning: `network` and `address` will be upgraded to POJOs in the future
 */
public class NetworkInfo {
    private final int version;
    private final String subVersion;
    private final int protocolVersion;
    private final int timeOffset;
    private final int connections;
    private final String proxy;
    private final int relayFee;
    private final byte[] localServices;
    private final Object[]  network;
    private final Object[]  address;

    @JsonCreator
    public NetworkInfo(@JsonProperty("version")         int version,
                       @JsonProperty("subversion")      String subVersion,
                       @JsonProperty("protocolversion") int protocolVersion,
                       @JsonProperty("timeOffset")      int timeOffset,
                       @JsonProperty("connections")     int connections,
                       @JsonProperty("proxy")           String proxy,
                       @JsonProperty("relayFee")        int relayFee,
                       @JsonProperty("localServices")   byte[] localServices,
                       @JsonProperty("network")         Object[] network,
                       @JsonProperty("address")         Object[] address) {
        this.version = version;
        this.subVersion = subVersion;
        this.protocolVersion = protocolVersion;
        this.timeOffset = timeOffset;
        this.connections = connections;
        this.proxy = proxy;
        this.relayFee = relayFee;
        this.localServices = localServices;
        this.network = network;
        this.address = address;
    }

    public int getVersion() {
        return version;
    }

    public String getSubVersion() {
        return subVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public int getConnections() {
        return connections;
    }

    public String getProxy() {
        return proxy;
    }

    public int getRelayFee() {
        return relayFee;
    }

    public byte[] getLocalServices() {
        return localServices;
    }

    public Object[] getNetwork() {
        return network;
    }

    public Object[] getAddress() {
        return address;
    }
}
