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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Minimal Descriptor implementation necessary for RegTest descriptor wallet support
 */
public class MinimalDescriptor {
    private final String desc;
    private final boolean active;
    private final Instant timestamp;
    private final boolean internal;


    public MinimalDescriptor(@JsonProperty("version") String desc,
                             @JsonProperty("active") boolean active,
                             @JsonProperty("timestamp") long timestamp,
                             @JsonProperty("internal") boolean internal) {
        this.desc = desc;
        this.active = active;
        this.timestamp = Instant.ofEpochSecond(timestamp);
        this.internal = internal;
    }

    public MinimalDescriptor(String desc, boolean active, long timestamp) {
        this(desc, active, timestamp, false);
    }

    public String getDesc() {
        return desc;
    }

    public boolean getActive() {
        return active;
    }

    public long getTimestamp() {
        return timestamp.getEpochSecond();
    }

    public boolean isInternal() {
        return internal;
    }
}


