package com.msgilligan.bitcoinj.rpcserver;

import com.msgilligan.bitcoinj.json.pojo.ServerInfo;

/**
 * Standard Bitcoin JSON-RPC service
 */
public interface BitcoinJsonRpc {
    Integer getblockcount();
    Integer getconnectioncount();
    ServerInfo getinfo();
}
