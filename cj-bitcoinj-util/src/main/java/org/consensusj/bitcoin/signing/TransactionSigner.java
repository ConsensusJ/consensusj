package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.consensusj.bitcoin.signing.internal.PatchedTransaction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 *
 */
public interface TransactionSigner {
    /**
     * Create a signed bitcoinj transaction from the signing request
     * <p>
     * @param request Signing request with data for all inputs and all outputs
     * @return A signed transaction (should be treated as immutable)
     */
    default CompletableFuture<Transaction> signTransaction(SigningRequest request) {
        // Create a new, empty (mutable) bitcoinj transaction
        PatchedTransaction transaction = new PatchedTransaction(NetworkParameters.fromID(request.networkId()));

        // For each output in the signing request, add an output to the bitcoinj transaction
        // TODO: Transaction validation
        request.outputs().forEach(
                output -> transaction.addOutput(output.toMutableOutput())
        );

        // For each address in the input list, add a signed input to the bitcoinj transaction
        request.inputs().forEach(
                input -> addSignedInput(transaction, input, () -> new RuntimeException("Unsupported transaction input"))
        );

        return CompletableFuture.completedFuture(transaction);
    }

    /**
     *
     * @param tx Mutable transaction currently being built
     * @param in The transaction input data
     * @param exceptionSupplier exception to throw if key is not available.
     */
    default void addSignedInput(Transaction tx, TransactionInputData in, Supplier<? extends RuntimeException> exceptionSupplier) {
        ((PatchedTransaction) tx).addSignedInput(in.toOutPoint(), in.script(), in.amount(), keyForInput(in).orElseThrow(exceptionSupplier), Transaction.SigHash.ALL, false);
    }

    /**
     * Return an ECKey, if available for the provided input
     * @param input A transaction input
     * @return The ECKey or empty
     */
    Optional<ECKey> keyForInput(TransactionInputData input);
}
