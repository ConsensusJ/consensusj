package org.consensusj.jsonrpc.groovy

import org.consensusj.jsonrpc.JsonRpcClient

/**
 * Trait for adding dynamic RPC method fallback to any RPC client
 */
trait DynamicRpcMethodFallback implements JsonRpcClient {
    /**
     * Dynamically forward missing method calls to the server
     *
     * See http://groovy-lang.org/metaprogramming.html#_methodmissing
     *
     * @param name The JSON-RPC method name
     * @param args JSON-RPC arguments
     * @return an object containing the JSON-RPC response.result
     * @throws org.consensusj.jsonrpc.JsonRpcStatusException
     */
    def methodMissing(String name, def args) {
        Object result = this.send(name, args as List)
        return result
    }

}