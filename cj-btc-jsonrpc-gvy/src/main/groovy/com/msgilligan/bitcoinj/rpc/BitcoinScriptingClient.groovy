package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile
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
}
