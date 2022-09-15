package org.consensusj.bitcoin.test

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.jsonrpc.groovy.test.BTCTestSupport
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import org.consensusj.jsonrpc.groovy.Loggable
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.bitcoinj.core.Coin
import spock.lang.Specification
import org.consensusj.bitcoin.jsonrpc.test.TestServers


/**
 * Abstract Base class for Spock tests of Bitcoin Core in RegTest mode
 */
abstract class BaseRegTestSpec extends Specification implements BTCTestSupport, Loggable {
    static final Coin minBTCForTests = 50.btc
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    private static BitcoinExtendedClient INSTANCE;

    static BitcoinExtendedClient getClientInstance() {
        // We use a shared client for RegTest integration tests, because we want a single value for regTestMiningAddress
        if (INSTANCE == null) {
            INSTANCE = new BitcoinExtendedClient(RpcURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)
        }
        return INSTANCE;
    }
    
    // Initializer to set up trait properties, Since Spock doesn't allow constructors
    {
        client = getClientInstance()
        serverReady(RegTestParams.get())
        fundingSource = new RegTestFundingSource(client)
    }

    void setupSpec() {
        serverReady(RegTestParams.get())

        // Make sure we have enough test coins
        // Do we really need to keep doing this now that most tests
        // explicitly fund their addresses?
        while (client.getBalance() < minBTCForTests) {
            // Mine blocks until we have some coins to spend
            client.generateBlocks(1)
        }
    }

    /**
     * Clean up after all tests in spec have run.
     */
    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

}