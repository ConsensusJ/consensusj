package org.consensusj.bitcoin.test

import groovy.util.logging.Slf4j
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.bitcoinj.params.RegTestParams
import org.consensusj.bitcoin.jsonrpc.groovy.test.BTCTestSupport
import org.consensusj.bitcoin.jsonrpc.test.FundingSource
import org.consensusj.bitcoin.jsonrpc.test.FundingSourceAccessor
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import org.consensusj.bitcoin.jsonrpc.RpcURI
import spock.lang.Specification
import org.consensusj.bitcoin.jsonrpc.test.TestServers

/**
 * Abstract Base class for Spock tests of Bitcoin Core in RegTest mode
 */
@Slf4j
abstract class BaseRegTestSpec extends Specification implements BTCTestSupport, FundingSourceAccessor {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    private static BitcoinExtendedClient INSTANCE;
    private static RegTestFundingSource FUNDING_INSTANCE;

    @Delegate
    @Override
    BitcoinExtendedClient client() {
        return getClientInstance()
    }

    @Delegate
    @Override
    FundingSource fundingSource() {
        if (FUNDING_INSTANCE == null) {
            FUNDING_INSTANCE = new RegTestFundingSource(client())
        }
        return FUNDING_INSTANCE;
    }

    /*package */ static BitcoinExtendedClient getClientInstance() {
        // We use a shared client for RegTest integration tests, because we want a single value for regTestMiningAddress
        if (INSTANCE == null) {
            INSTANCE = new BitcoinExtendedClient(RpcURI.getDefaultRegTestWalletURI(), rpcTestUser, rpcTestPassword)
        }
        return INSTANCE;
    }
    
    void setupSpec() {
        serverReady(RegTestParams.get())
        client().initRegTestWallet()
    }

    /**
     * Clean up after all tests in spec have run.
     */
    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

    void consolidateCoins() {
        fundingSource.fundingSourceMaintenance();
    }
}
