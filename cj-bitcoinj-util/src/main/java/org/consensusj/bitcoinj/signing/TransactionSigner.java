package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.DeterministicKeyChain;

import java.util.concurrent.CompletableFuture;

/**
 * A low-level transaction signing interface that can sign a {@link SigningRequest} that is a complete
 * transaction that only needs signatures. The {@link HDKeychainSigner} implementation can sign transactions
 * using a <b>bitcoinj</b> {@link DeterministicKeyChain}.
 */
public interface TransactionSigner {
    /**
     * Create a signed bitcoinj transaction from the signing request. This is asynchronous because
     * user (or other) confirmation may be required.
     * @param request Signing request with data for all inputs and all outputs
     * @return A signed transaction (should be treated as immutable)
     */
    CompletableFuture<Transaction> signTransaction(SigningRequest request);
}
