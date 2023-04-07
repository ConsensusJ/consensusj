package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

/**
 * A source of Bitcoin funds for testing
 *
 * In RegTest mode, it can be a RegTestFundingSource that mines coins in RegTest mode and sends them
 * to a requesting address. In other modes it can be a TestWallet preloaded with a certain amount of coins.
 */
public interface FundingSource {
    Sha256Hash requestBitcoin(Address toAddress, Coin requestedAmount) throws Exception;
    Address createFundedAddress(Coin amount) throws Exception;

    /**
     * An opportunity to do any necessary housekeeping. (e.g. consolidation)
     */
    void fundingSourceMaintenance();
}
