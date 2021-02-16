package com.msgilligan.bitcoinj.rpcserver;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.NetworkInfo;
import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import org.bitcoinj.core.Sha256Hash;

/**
 * Standard Bitcoin JSON-RPC service
 */
public interface BitcoinJsonRpc {
    Integer getblockcount();
    Sha256Hash getbestblockhash();
    Object getblockheader(String blockHashString, Boolean verbose);
// TODO: Support Sha256Hash type in request params?
//    Object getblock(Sha256Hash blockHash, Integer verbosity);
    Object getblock(String blockHashString, Integer verbosity);
    Sha256Hash getblockhash(Integer blockNumber);
    Integer getconnectioncount();
    @Deprecated
    ServerInfo getinfo();
    BlockChainInfo getblockchaininfo();
    NetworkInfo getnetworkinfo();
}
