package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile

/**
 * Bitcoin RPC client for scripting
 * No args constructor reads bitcoin.conf
 * Allows dynamic methods to access new RPCs
 */
class BitcoinScriptingClient extends BitcoinExtendedClient implements DynamicRPCFallback {

    BitcoinScriptingClient() {
        super(BitcoinConfFile.readDefaultConfig().getRPCConfig())
    }
}
