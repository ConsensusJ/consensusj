package com.msgilligan.ethereum.rpc

import org.consensusj.jsonrpc.groovy.DynamicRPCFallback

/**
 * Use `methodMissing` to implement Ethereum RPC calls dynamically
 */
class EthereumScriptingClient extends EthereumClient implements DynamicRPCFallback {
    EthereumScriptingClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
    }
}
