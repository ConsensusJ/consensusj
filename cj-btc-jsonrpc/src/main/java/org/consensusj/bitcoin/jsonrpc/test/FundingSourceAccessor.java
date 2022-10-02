package org.consensusj.bitcoin.jsonrpc.test;

/**
 * Interface for tests that use a FundingSource
 */
public interface FundingSourceAccessor {
    /**
     * Preferred accessor
     * @return The FundingSource
     */
    FundingSource fundingSource();

    /**
     * JavaBeans style getter/accessor (for Groovy, etc)
     * @return The FundingSource
     */
    default FundingSource getFundingSource() {
        return fundingSource();
    }
}
