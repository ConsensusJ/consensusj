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
import org.bitcoinj.base.Coin;

import java.math.BigDecimal;

/**
 * POJO for `getinfo` RPC response.
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
