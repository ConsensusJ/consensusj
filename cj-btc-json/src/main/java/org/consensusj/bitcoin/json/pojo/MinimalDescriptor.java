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


