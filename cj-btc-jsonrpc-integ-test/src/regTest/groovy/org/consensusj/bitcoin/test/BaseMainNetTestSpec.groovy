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
package org.consensusj.bitcoin.test

import groovy.util.logging.Slf4j
import org.bitcoinj.base.BitcoinNetwork
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.jsonrpc.test.BTCTestSupport
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.TestServers
import spock.lang.Specification

/**
 * Abstract Base class for Spock tests of Bitcoin Core on MainNet
 */
@Slf4j
abstract class BaseMainNetTestSpec extends Specification implements BTCTestSupport {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    private static BitcoinExtendedClient INSTANCE;

    @Delegate
    @Override
    BitcoinExtendedClient client() {
        return getClientInstance();
    }

    static BitcoinExtendedClient getClientInstance() {
        // We use a shared client for integration tests, to avoid repeated configuration fetch from server
        if (INSTANCE == null) {
            INSTANCE = new BitcoinExtendedClient(RpcURI.defaultMainNetURI, rpcTestUser, rpcTestPassword)
        }
        return INSTANCE;
    }

    void setupSpec() {
        serverReady(BitcoinNetwork.MAINNET)
    }

    /**
     * Clean up after all tests in spec have run.
     */
    void cleanupSpec() {
    }
}
