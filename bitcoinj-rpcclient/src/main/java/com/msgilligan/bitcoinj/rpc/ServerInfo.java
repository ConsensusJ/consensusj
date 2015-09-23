package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Coin;

import java.math.BigDecimal;

/**
 *
 */
public class ServerInfo {
    private final int version;
    private final int protocolversion;
    private final int walletversion;
    private final Coin balance;
    private final int blocks;
    private final int timeoffset;
    private final int connections;
    private final String proxy;
    private final BigDecimal difficulty;
    private final Boolean testnet;
    private final int keypoololdest;
    private final int keypoolsize;
    private final Coin paytxfee;
    private final Coin relayfee;
    private final String errors;

    @JsonCreator
    public ServerInfo(@JsonProperty("version")          int version,
                      @JsonProperty("protocolversion")  int protocolversion,
                      @JsonProperty("walletversion")    int walletversion,
                      @JsonProperty("balance")          Coin balance,
                      @JsonProperty("blocks")           int blocks,
                      @JsonProperty("timeoffset")       int timeoffset,
                      @JsonProperty("connections")      int connections,
                      @JsonProperty("proxy")            String proxy,
                      @JsonProperty("difficulty")       BigDecimal difficulty,
                      @JsonProperty("testnet")          Boolean testnet,
                      @JsonProperty("keypoololdest")    int keypoololdest,
                      @JsonProperty("keypoolsize")      int keypoolsize,
                      @JsonProperty("paytxfee")         Coin paytxfee,
                      @JsonProperty("relayfee")         Coin relayfee,
                      @JsonProperty("errors")           String errors) {
        this.version = version;
        this.protocolversion = protocolversion;
        this.walletversion = walletversion;
        this.balance = balance;
        this.blocks = blocks;
        this.timeoffset = timeoffset;
        this.connections = connections;
        this.proxy = proxy;
        this.difficulty = difficulty;
        this.testnet = testnet;
        this.keypoololdest = keypoololdest;
        this.keypoolsize = keypoolsize;
        this.paytxfee = paytxfee;
        this.relayfee = relayfee;
        this.errors = errors;
    }

    public int getVersion() {
        return version;
    }

    public int getProtocolversion() {
        return protocolversion;
    }

    public int getWalletversion() {
        return walletversion;
    }

    public Coin getBalance() {
        return balance;
    }

    public int getBlocks() {
        return blocks;
    }

    public int getTimeoffset() {
        return timeoffset;
    }

    public int getConnections() {
        return connections;
    }

    public String getProxy() {
        return proxy;
    }

    public BigDecimal getDifficulty() {
        return difficulty;
    }

    public Boolean getTestnet() {
        return testnet;
    }

    public int getKeypoololdest() {
        return keypoololdest;
    }

    public int getKeypoolsize() {
        return keypoolsize;
    }

    public Coin getPaytxfee() {
        return paytxfee;
    }

    public Coin getRelayfee() {
        return relayfee;
    }

    public String getErrors() {
        return errors;
    }
}
