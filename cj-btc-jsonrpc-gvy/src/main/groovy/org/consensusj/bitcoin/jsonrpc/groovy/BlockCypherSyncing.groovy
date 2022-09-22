package org.consensusj.bitcoin.jsonrpc.groovy

import groovy.json.JsonSlurper
import org.consensusj.bitcoin.jsonrpc.test.BlockchainSyncing

/**
 * Implementation of BlockchainSyncing that uses BlockCypher.com API
 *
 * Note: 5 requests/sec and 600 requests/hr without API token.
 */
interface BlockCypherSyncing extends BlockchainSyncing {

    default int getReferenceBlockHeight() {
        int height = new JsonSlurper().parse(new URL("https://api.blockcypher.com/v1/btc/main")).height
        return height
    }
}
