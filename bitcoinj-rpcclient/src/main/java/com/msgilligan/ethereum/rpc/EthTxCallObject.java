package com.msgilligan.ethereum.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class EthTxCallObject {
    private final String from;
    private final String to;
    private final String gas;
    private final String gasPrice;
    private final String value;
    private final String data;

    public EthTxCallObject(@JsonProperty("from") String from,
                           @JsonProperty("to") String to,
                           @JsonProperty("gas") String gas,
                           @JsonProperty("gasPrice") String gasPrice,
                           @JsonProperty("value") String value,
                           @JsonProperty("data") String data) {
        this.from = from;
        this.to = to;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.value = value;
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getGas() {
        return gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getValue() {
        return value;
    }

    public String getData() {
        return data;
    }
}
