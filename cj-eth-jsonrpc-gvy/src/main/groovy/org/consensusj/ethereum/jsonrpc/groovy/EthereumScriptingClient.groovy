package org.consensusj.ethereum.rpc

import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback

/**
 * Use `methodMissing` to implement Ethereum RPC calls dynamically
 */
class EthereumScriptingClient extends EthereumClient implements DynamicRpcMethodFallback {
    EthereumScriptingClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
    }
}
