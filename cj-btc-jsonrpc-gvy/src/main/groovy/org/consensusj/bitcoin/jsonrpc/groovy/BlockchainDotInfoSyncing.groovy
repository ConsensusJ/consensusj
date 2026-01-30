/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
