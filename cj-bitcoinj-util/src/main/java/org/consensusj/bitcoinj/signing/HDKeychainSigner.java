package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicKeyChain;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A "signing wallet"  that uses a {@link DeterministicKeyChain} to
 * sign {@link SigningRequest}s.
 */
public class HDKeychainSigner implements BaseTransactionSigner {
    private final DeterministicKeyChain keyChain;

    public HDKeychainSigner(DeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
    }

    /**
     * Create a signed bitcoinj transaction from the signing request
     * <p>
     * NOTE: This was an attempt to use an unsigned transaction and a list of address.
     * It is private and should not be used. I'm leaving it here because it demonstrates
     * the use of `duplicateDetached()` and we may want to use this elsewhere to sign
     * incomplete bitcoinj transactions.
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

        // Create a new, empty bitcoinj transaction
        Transaction tx = new Transaction();

        // For each output in the signing request, add an output to the bitcoinj transaction
        // TODO: Transaction validation
        for (TransactionOutput output : unsignedTx.getOutputs()) {
            tx.addOutput(output.duplicateDetached());
        }

        // For each address in the input list, add a signed input to the bitcoinj transaction
        for (int index = 0; index < unsignedTx.getInputs().size(); index++) {
            Address address = addresses.get(index);
            TransactionInput input = unsignedTx.getInputs().get(index);
            TransactionOutPoint outPoint = input.getOutpoint().disconnectOutput();
            DeterministicKey fromKey = keyChain.findKeyFromPubHash(address.getHash());
            tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(address), input.getValue(), fromKey);
        }
        return CompletableFuture.completedFuture(tx);
    }

    /**
     * Return the signing key for an input, if available
     * @param input Transaction input data
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    public Optional<ECKey> keyForInput(TransactionInputData input) {

        return Optional.ofNullable(
                keyChain.findKeyFromPubHash(input.script().getPubKeyHash())
        );
    }

    /**
     * Return the signing key for an input, if available
     * @param pubKeyHash pubKeyHash
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    public Optional<ECKey> keyForHash(byte[] pubKeyHash) {

        return Optional.ofNullable(
                keyChain.findKeyFromPubHash(pubKeyHash)
        );
    }

    public Optional<ECKey> pubKeyFromPubKeyHash(byte[] pubKeyHash) {
        return keyForHash(pubKeyHash).map(ECKey::fromPublicOnly);
    }
}
