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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 * Information about an active ZeroMQ notification. An array of this type is returned by the {@code getzmqnotifications} JSON-RPC method.
 */
public class ZmqNotification {
    private final String type;
    private final URI address;
    private final int hwm;

    @JsonCreator
    public ZmqNotification(@JsonProperty("type")    String type,
                           @JsonProperty("address") String address,
                           @JsonProperty("hwm")     int hwm) {
        this.type = type;
        this.address = URI.create(address);
        this.hwm = hwm;
    }

    /**
     * @return Type of notification
     */
    public String type() {
        return type;
    }

    /**
     * @return Address of the publisher
     */
    public URI address() {
        return address;
    }

    /**
     * @return Outbound message high water mark
     */
    public int hwm() {
        return hwm;
    }

    @Deprecated public String getType() { return type(); }
    @Deprecated public URI getAddress() { return address(); }
    @Deprecated public int getHwm() { return hwm(); }
}
