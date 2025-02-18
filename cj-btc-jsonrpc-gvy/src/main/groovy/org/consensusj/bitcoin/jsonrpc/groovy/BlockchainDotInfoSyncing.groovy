package org.consensusj.bitcoin.jsonrpc.groovy

import groovy.json.JsonSlurper
import org.consensusj.bitcoin.jsonrpc.test.BlockchainSyncing

/**
 * Implementation of BlockchainSyncing that uses Blockchain.info API
 */
interface BlockchainDotInfoSyncing extends BlockchainSyncing {

    default int getReferenceBlockHeight() {
        URL latestBlockUrl = URI.create("https://blockchain.info/latestblock").toURL()
        int height = new JsonSlurper().parse(latestBlockUrl).height
        return height
    }
}
