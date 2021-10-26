package org.consensusj.bitcoin.test

import org.consensusj.bitcoin.rpc.BitcoinExtendedClient
import org.bitcoinj.params.MainNetParams
import org.consensusj.jsonrpc.groovy.Loggable
import org.consensusj.bitcoin.rpc.RpcURI
import org.consensusj.bitcoin.rpc.test.TestServers
import org.consensusj.bitcoin.test.BTCTestSupport
import spock.lang.Specification

/**
 * Abstract Base class for Spock tests of Bitcoin Core on MainNet
 */
abstract class BaseMainNetTestSpec extends Specification implements BTCTestSupport, Loggable {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;

    // Initializer to set up trait properties, Since Spock doesn't allow constructors
    {
        client = new BitcoinExtendedClient(MainNetParams.get(), RpcURI.defaultMainNetURI, rpcTestUser, rpcTestPassword)
        fundingSource = null    // No funding source implementation for MainNet yet.
    }

    void setupSpec() {
        serverReady()
    }

    /**
     * Clean up after all tests in spec have run.
     */
    void cleanupSpec() {
    }

}