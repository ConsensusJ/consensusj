package com.msgilligan.ethereum.rpc

import com.msgilligan.jsonrpc.DynamicRPCFallback

/**
 * Use `methodMissing` to implement Ethereum RPC calls dynamically
 */
class EthereumScriptingClient extends EthereumClient implements DynamicRPCFallback {
}
