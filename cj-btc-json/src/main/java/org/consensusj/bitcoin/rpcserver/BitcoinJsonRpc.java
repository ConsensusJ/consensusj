package org.consensusj.bitcoin.rpcserver;

import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
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
    BlockChainInfo getblockchaininfo();
    NetworkInfo getnetworkinfo();
}
