package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

import java.util.Arrays;
import java.util.Optional;

/**
 * A simple transaction signer using a single ECKey that can sign either
 * {@link ScriptType#P2PKH} or {@link ScriptType#P2WPKH} transactions.
 */
public class ECKeySigner implements TransactionSigner {
    private final ECKey ecKey;

    /**
     * Construct a signer from a single key
     * @param ecKey signing key
     */
    public ECKeySigner(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    /**
     * Return the signing key for an input, if available
     * @param input Transaction input data
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    public Optional<ECKey> keyForInput(TransactionInputData input) {
        Script s = input.script();
        return  ((ScriptPattern.isP2PKH(s) || ScriptPattern.isP2WPKH(s)) && Arrays.equals(s.getPubKeyHash(), ecKey.getPubKeyHash()))
                ? Optional.of(ecKey)
                : Optional.empty();
    }
}
