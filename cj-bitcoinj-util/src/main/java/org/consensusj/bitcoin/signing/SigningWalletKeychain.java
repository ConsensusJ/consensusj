package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.ScriptBuilder;
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A "signing wallet"  that uses a {@link BipStandardDeterministicKeyChain} to
 * sign {@link SigningRequest}s.
 */
public class SigningWalletKeychain {
    private final BipStandardDeterministicKeyChain keyChain;

    public SigningWalletKeychain(BipStandardDeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
    }

    /**
     * Create a signed bitcoinj transaction from the signing request
     * <p>
     * NOTE: This was an attempt to use an unsigned transaction and a list of address.
     * It is private and should not be used. I'm leaving it here because it demonstrates
     * the use of `duplicatDetached()` and we may want to use this elsewhere to sign
     * incomplete bitcionj transactions.
     * <p>
     * Assumes all inputs are P2PKH (for now) and addresses must be in
     * the same order as the unsigned inputs in the unsigned transaction.
     * 
     * @param addresses An ordered list of funding addresses (keys must be in HD keychain)
     * @param unsignedTx An unsigned bitcoinj transaction
     * @return a newly created, signed bitcoinj transaction
     */
    private CompletableFuture<Transaction> signTransaction(List<Address> addresses, Transaction unsignedTx) {
        if (addresses.size() != unsignedTx.getInputs().size()) {
            throw new IllegalArgumentException("addresses and inputs must be 1:1 mapped");
        }
        NetworkParameters netParams = unsignedTx.getParams();

        // Create a new, empty bitcoinj transaction
        Transaction tx = new Transaction(netParams);

        // For each output in the signing request, add an output to the bitcoinj transaction
        // TODO: Transaction validation
        for (TransactionOutput output : unsignedTx.getOutputs()) {
            tx.addOutput(output.duplicateDetached());
        }

        // For each address in the input list, add a signed input to the bitcoinj transaction
        for (int index = 0; index < unsignedTx.getInputs().size(); index++) {
            Address address = addresses.get(index);
            TransactionOutPoint outPoint = unsignedTx.getInputs().get(index).duplicateDetached().getOutpoint();
            DeterministicKey fromKey = keyChain.findKeyFromPubHash(address.getHash());
            tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(address), fromKey);
        }
        return CompletableFuture.completedFuture(tx);
    }


    /**
     * Create a signed bitcoinj transaction from the signing request
     * <p>
     * @param request Signing request with data for all inputs and all outputs
     * @return A signed transaction (should be treated as immutable)
     */
    public CompletableFuture<Transaction> signTransaction(SigningRequest request) {
        // Create a new, empty (mutable) bitcoinj transaction
        Transaction transaction = new Transaction(NetworkParameters.fromID(request.networkId()));

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
    void addSignedInput(Transaction tx, TransactionInputData in, Supplier<? extends RuntimeException> exceptionSupplier) {
        tx.addSignedInput(in.toOutPoint(), in.script(), keyForInput(in).orElseThrow(exceptionSupplier));
    }

    /**
     * Return the signing key for an input, if available
     * @param input Transaction input data
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    Optional<DeterministicKey> keyForInput(TransactionInputData input) {
        return input
                .address()
                .map(a -> keyChain.findKeyFromPubHash(a.getHash()));
    }
}
