package org.consensusj.bitcoin.jsonrpc.groovy.test

import org.consensusj.bitcoin.jsonrpc.test.FundingSource
import org.consensusj.bitcoin.jsonrpc.test.FundingSourceAccessor

/**
 * Provide a funding source for a test Spec
 */
@Deprecated
trait FundingSourceDelegate implements FundingSourceAccessor {
    @Delegate
    FundingSource fundingSource

    /**
     * Since we can't have a final (read-only) property in a trait
     * Let's at least allow it to be only set once.
     */
    void setFundingSource(FundingSource fundingSource) {
        if (this.fundingSource == null) {
            this.fundingSource = fundingSource
        } else {
            throw new RuntimeException("FundingSource already set")
        }
    }

}