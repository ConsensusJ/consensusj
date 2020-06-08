package com.msgilligan.bitcoinj.rpcserver;

import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import org.bitcoinj.core.Sha256Hash;

/**
 * Standard Bitcoin JSON-RPC service
 */
public interface BitcoinJsonRpc {
    Integer getblockcount();
// TODO: Support Sha256Hash type in request params?
//    Object getblock(Sha256Hash blockHash, Integer verbosity);
    Object getblock(String blockHashString, Integer verbosity);
    Sha256Hash getblockhash(Integer blockNumber);
    Integer getconnectioncount();
    ServerInfo getinfo();
}
