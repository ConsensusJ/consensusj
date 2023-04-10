package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.BitcoinNetworkParams;

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
        NetworkParameters params = BitcoinNetworkParams.fromID(request.networkId());
        // Create a new, empty (mutable) bitcoinj transaction
        Transaction transaction = new Transaction(params);

        // For each output in the signing request, add an output to the bitcoinj transaction
        // TODO: Transaction validation
        request.outputs().forEach(
                output -> transaction.addOutput(output.toMutableOutput(params.network()))
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
        tx.addSignedInput(in.toOutPoint(tx.getParams().network()), in.script(), in.amount(), keyForInput(in).orElseThrow(exceptionSupplier), Transaction.SigHash.ALL, false);
    }

    /**
     * Return an ECKey, if available for the provided input
     * @param input A transaction input
     * @return The ECKey or empty
     */
    Optional<ECKey> keyForInput(TransactionInputData input);
}
