package com.msgilligan.namecoinj.rpc

import org.consensusj.jsonrpc.groovy.DynamicRPCFallback

/**
 * Namecoin RPC client for scripting
 * No args constructor reads namecoin.conf
 * Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class NamecoinScriptingClient extends NamecoinClient implements DynamicRPCFallback {
    public NamecoinScriptingClient() {
        super(readConfig());
    }
}
