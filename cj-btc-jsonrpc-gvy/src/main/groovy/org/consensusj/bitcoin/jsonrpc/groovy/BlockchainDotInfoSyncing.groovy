package org.consensusj.bitcoin.jsonrpc.groovy

import groovy.json.JsonSlurper

/**
 * Implementation of BlockchainSyncing that uses Blockchain.info API
 */
interface BlockchainDotInfoSyncing extends BlockchainSyncing {

    default int getReferenceBlockHeight() {
        int height = new JsonSlurper().parse(new URL("https://blockchain.info/latestblock")).height
        return height
    }
}
