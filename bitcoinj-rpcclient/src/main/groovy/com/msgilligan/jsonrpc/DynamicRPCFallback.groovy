package com.msgilligan.jsonrpc

/**
 * Trait for adding dynamic RPC method fallback to any RPC client
 */
trait DynamicRPCFallback implements UntypedRPCClient {
    /**
     * Dynamically forward missing method calls to the server
     *
     * See http://groovy-lang.org/metaprogramming.html#_methodmissing
     *
     * @param name The JSON-RPC method name
     * @param args JSON-RPC arguments
     * @return an object containing the JSON-RPC response.result
     * @throws com.msgilligan.jsonrpc.JsonRPCStatusException
     */
    def methodMissing(String name, def args) {
        Object result = this.send(name, args as List)
        return result
    }

}