package org.consensusj.bitcoin.jsonrpc.groovy

import groovy.transform.CompileStatic
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.jsonrpc.BitcoinClient

/**
 * Bitcoin JSON-RPC client with method names that exactly match wire and CLI names.
 *
 * Currently incomplete and unused. Should this extend BitcoinClient or wrap it?
 */
@CompileStatic
class BitcoinCLIClient extends BitcoinClient {

    BitcoinCLIClient(URI server, String rpcuser, String rpcpassword) {
        super(RegTestParams.get(), server, rpcuser, rpcpassword)
    }
}
