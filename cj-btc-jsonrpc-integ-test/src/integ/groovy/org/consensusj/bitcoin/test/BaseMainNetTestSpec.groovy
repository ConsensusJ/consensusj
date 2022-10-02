package org.consensusj.bitcoin.test

import groovy.util.logging.Slf4j
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.bitcoinj.params.MainNetParams
import org.consensusj.bitcoin.jsonrpc.groovy.test.BTCTestSupport
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
        serverReady(MainNetParams.get())
    }

    /**
     * Clean up after all tests in spec have run.
     */
    void cleanupSpec() {
    }
}
