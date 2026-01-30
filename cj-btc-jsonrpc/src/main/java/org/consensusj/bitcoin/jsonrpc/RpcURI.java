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
