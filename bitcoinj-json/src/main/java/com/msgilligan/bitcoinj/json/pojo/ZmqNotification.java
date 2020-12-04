package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 * Information on ZMQ Notifications, returned in an array by `getzmqnotifications` RPC
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

    public String getType() {
        return type;
    }

    public URI getAddress() {
        return address;
    }

    public int getHwm() {
        return hwm;
    }
}
