package org.consensusj.bitcoin.jsonrpc.groovy

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile
import org.bitcoinj.core.NetworkParameters
import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback

/**
 * Bitcoin RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class BitcoinScriptingClient extends BitcoinExtendedClient implements DynamicRpcMethodFallback {

    BitcoinScriptingClient() {
        super(BitcoinConfFile.readDefaultConfig().getRPCConfig())
    }

    BitcoinScriptingClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword);
    }
}
