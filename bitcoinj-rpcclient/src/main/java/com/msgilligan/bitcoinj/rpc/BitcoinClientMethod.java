package com.msgilligan.bitcoinj.rpc;

/**
 * Enumerated type of known Bitcoin RPC methods
 */
public enum BitcoinClientMethod implements JSONRPCMethod {
        getblockcount,
        getblockhash,
        @Deprecated
        setgenerate,
        generate
}
