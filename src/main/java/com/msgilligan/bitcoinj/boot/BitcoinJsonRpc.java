package com.msgilligan.bitcoinj.boot;

import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 * Standard Bitcoin JSON-RPC service
 */
//@JsonRpcService("BitcoinRPC")
public interface BitcoinJsonRpc {
    public Integer getblockcount();
    public Integer getconnectioncount();
}
