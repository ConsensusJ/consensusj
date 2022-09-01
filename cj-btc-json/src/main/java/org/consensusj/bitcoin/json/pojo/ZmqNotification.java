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
