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
package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Network;
import org.consensusj.jsonrpc.JsonRpcException;

import java.time.Duration;

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
public interface BTCTestSupport extends BitcoinClientAccessor {

    /**
     * Wait for the server to become ready and validate the Bitcoin network it is running on
     * @param expectedNetwork The network the server is expected to be running on
     */
    default void serverReady(Network expectedNetwork) throws JsonRpcException {
        Boolean ready = client().waitForServer(Duration.ofSeconds(60));   // Wait up to 1 minute
        if (!ready) {
            throw new RuntimeException("Timeout waiting for server");
        }
        // As soon as the server is ready we must initialize network parameters in the client (by querying them)
        Network network = client().getNetwork();
        if (!network.equals(expectedNetwork)) {
            throw new IllegalStateException("Server reports unexpected Bitcoin network.");
        }
    }
}
