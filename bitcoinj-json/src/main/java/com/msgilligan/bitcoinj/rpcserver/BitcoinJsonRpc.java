package com.msgilligan.bitcoinj.rpcserver;

/**
 * Standard Bitcoin JSON-RPC service
 */
//@JsonRpcService("BitcoinRPC")
public interface BitcoinJsonRpc {
    public Integer getblockcount();
    public Integer getconnectioncount();
}
