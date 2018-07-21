package org.consensusj.namecoin.jsonrpc.groovy

import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback
import org.consensusj.namecoin.jsonrpc.NamecoinClient

/**
 * Namecoin RPC client for scripting
 * No args constructor reads namecoin.conf
 * Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class NamecoinScriptingClient extends NamecoinClient implements DynamicRpcMethodFallback {
    public NamecoinScriptingClient() {
        super(readConfig());
    }
}
