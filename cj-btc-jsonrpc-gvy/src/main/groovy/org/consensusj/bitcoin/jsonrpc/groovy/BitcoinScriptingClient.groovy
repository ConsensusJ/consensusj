package org.consensusj.bitcoin.jsonrpc.groovy

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile
import org.bitcoinj.core.NetworkParameters
import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback

/**
 * Bitcoin RPC client for scripting. Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class BitcoinScriptingClient extends BitcoinExtendedClient implements DynamicRpcMethodFallback {

    /**
     * No args constructor reads bitcoin.conf
     */
    BitcoinScriptingClient() {
        super()
    }

    BitcoinScriptingClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword);
    }
}
