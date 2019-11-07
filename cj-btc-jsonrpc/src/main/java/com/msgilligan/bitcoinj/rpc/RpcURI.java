package com.msgilligan.bitcoinj.rpc;

import java.net.URI;

/**
 * Utility class with default connection information for Bitcoin JSON-RPC
 */
public class RpcURI {
    public static final String rpcproto = "http";
    public static final String rpcssl = "https";
    public static final String rpchost = "127.0.0.1";
    public static final String rpcfile = "/";

    public static final int RPCPORT_MAINNET = 8332;
    public static final int RPCPORT_TESTNET = 18332;
    public static final int RPCPORT_REGTEST = 18443;  // Was same port as TESTNET until Bitcoin Core 0.16.0

    public static URI getDefaultMainNetURI() {
        return createURI(rpcproto, rpchost, RPCPORT_MAINNET, rpcfile);
    }

    public static URI getDefaultTestNetURI() {
        return createURI(rpcproto, rpchost, RPCPORT_TESTNET, rpcfile);
    }

    public static URI getDefaultRegTestURI() {
        return createURI(rpcproto, rpchost, RPCPORT_REGTEST, rpcfile);
    }

    private static URI createURI(String proto, String host, int port, String file) {
        return URI.create(rpcproto + "://" + rpchost + ":" + port + rpcfile);
    }

}
