package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.BitcoinNetworkParams;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.wallet.DeterministicKeyChain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A template interface, implementations need only implement {@link #keyForInput(TransactionInputData)}.
 * The {@link HDKeychainSigner} implementation can sign transactions using a <b>bitcoinj</b> {@link DeterministicKeyChain}.
 * <p>
 * Keys and PubKeys can be searched for, but not UTXOs or amounts
 */
public interface BaseTransactionSigner extends TransactionSigner {
    /**
     * Create a signed bitcoinj transaction from the signing request
     * <p>
     * @param request Signing request with data for all inputs and all outputs
     * @return A signed transaction (should be treated as immutable)
     */
    @Override
    default CompletableFuture<Transaction> signTransaction(SigningRequest request) {
        NetworkParameters params = BitcoinNetworkParams.of(request.network());
        // Create a new, empty (mutable) bitcoinj transaction
        Transaction transaction = new Transaction(params);

        // For each output in the signing request, add an output to the bitcoinj transaction
        request.outputs().forEach(
                output -> transaction.addOutput(output.toMutableOutput(params.network()))
        );

        // For each address in the input list, add a signed input to the bitcoinj transaction
        request.inputs().forEach(
                input -> addSignedInput(transaction, input, () -> new RuntimeException("Unsupported transaction input"))
        );

        // TODO: Additional Transaction validation?
        return verify(transaction, request.inputs())
                .map(CompletableFuture::<Transaction>failedFuture)
                .orElse(CompletableFuture.completedFuture(transaction));
    }

    default Optional<Exception> verify(Transaction tx, List<TransactionInputData> reqInputs) {
        return tx.getInputs().stream().flatMap(i ->
            verifyInput(tx, i.getIndex(), reqInputs.get(i.getIndex())).stream()
        ).findFirst();
    }

    default Optional<Exception> verifyInput(Transaction tx, int index, TransactionInputData inputData) {
        try {
            TransactionVerification.correctlySpendsInput(tx, index, inputData.script());
            return Optional.empty();
        } catch (ScriptException se) {
            return Optional.of(se);
        }
    }

    /**
     *
     * @param tx Mutable transaction currently being built
     * @param in The transaction input data
     * @param exceptionSupplier exception to throw if key is not available.
     */
    default void addSignedInput(Transaction tx, TransactionInputData in, Supplier<? extends RuntimeException> exceptionSupplier) {
        tx.addSignedInput(in.toOutPoint(tx.getParams().network()),
                in.script(),
                in.amount(),
                keyForInput(in).orElseThrow(exceptionSupplier),
                Transaction.SigHash.ALL,
                false);
    }

    /**
     * Return an ECKey, if available for the provided input
     * @param input A transaction input
     * @return The ECKey or empty
     */
    Optional<ECKey> keyForInput(TransactionInputData input);

    default Optional<ECKey> pubKeyForInput(TransactionInputData input) {
        return keyForInput(input).map(ECKey::fromPublicOnly);
    }
}
