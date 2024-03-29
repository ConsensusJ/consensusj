package org.consensusj.bitcoin.jsonrpc.groovy

import com.fasterxml.jackson.databind.JavaType
import org.bitcoinj.base.Network
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback

/**
 * Bitcoin RPC client for scripting. Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class BitcoinScriptingClient extends BitcoinExtendedClient implements DynamicRpcMethodFallback<JavaType> {

    /**
     * No args constructor reads bitcoin.conf
     */
    BitcoinScriptingClient() {
        super()
    }

    BitcoinScriptingClient(Network network, URI server, String rpcuser, String rpcpassword) {
        super(network, server, rpcuser, rpcpassword);
    }
}
