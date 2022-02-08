package org.consensusj.bitcoin.signing;

import java.util.List;

/**
 * A transaction signing request with complete immutable data specifying the transaction.
 */
public interface SigningRequest {
    String networkId();
    List<TransactionInputData> inputs();
    List<TransactionOutputData> outputs();
}
