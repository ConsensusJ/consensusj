package com.msgilligan.bitcoinj.rpc

import groovy.json.JsonSlurper

/**
 * Implementation of BlockchainSyncing that uses BlockCypher.com API
 *
 * Note: 5 requests/sec and 600 requests/hr without API token.
 */
trait BlockCypherSyncing extends BlockchainSyncing {

    Integer getReferenceBlockHeight() {
        Integer height = new JsonSlurper().parse(new URL("https://api.blockcypher.com/v1/btc/main")).height
        return height
    }
}