package org.consensusj.bitcoin.jsonrpc;

import java.net.URI;

/**
 * Interface with default connection information for Bitcoin JSON-RPC
 */
public interface RpcURI {
    String rpcproto = "http";
    String rpcssl = "https";
    String rpchost = "127.0.0.1";
    String rpcfile = "/";

    int RPCPORT_MAINNET = 8332;
    int RPCPORT_TESTNET = 18332;
    int RPCPORT_REGTEST = 18443;  // Was same port as TESTNET until Bitcoin Core 0.16.0

    URI DEFAULT_MAINNET_URI = rpcURI( rpchost, RPCPORT_MAINNET);
    URI DEFAULT_TESTNET_URI = rpcURI( rpchost, RPCPORT_TESTNET );
    URI DEFAULT_REGTEST_URI = rpcURI( rpchost, RPCPORT_REGTEST);

    // TODO: Deprecate?
    URI defaultMainNetURI = DEFAULT_MAINNET_URI;
    URI defaultTestNetURI = DEFAULT_TESTNET_URI;
    URI defaultRegTestURI = DEFAULT_REGTEST_URI;

    static URI getDefaultMainNetURI() {
        return DEFAULT_MAINNET_URI;
    }

    static URI getDefaultTestNetURI() {
        return DEFAULT_TESTNET_URI;
    }

    static URI getDefaultRegTestURI() {
        return DEFAULT_REGTEST_URI;
    }

    static URI getDefaultRegTestWalletURI() {
        return getRegTestWalletURI(rpchost);
    }

    static URI getRegTestWalletURI(String hostName) {
        return rpcWalletURI(hostName, RPCPORT_REGTEST, BitcoinExtendedClient.REGTEST_WALLET_NAME);
    }

    static URI rpcURI(String hostName, int port) {
        return URI.create(rpcproto + "://" + hostName + ":" + port + rpcfile);
    }

    static URI rpcWalletURI(String hostName, int port, String walletName) {
        return URI.create(rpcproto + "://" + hostName + ":" + port + "/wallet/" + walletName);
    }
}
