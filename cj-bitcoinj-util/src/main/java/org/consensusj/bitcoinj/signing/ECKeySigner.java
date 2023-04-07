package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.util.Optional;

/**
 * A simple transaction signer using a single ECKey.
 */
public class ECKeySigner implements TransactionSigner {
    private final ECKey ecKey;
    private final Address address;

    public ECKeySigner(NetworkParameters netParams, ECKey ecKey, ScriptType scriptType) {
        this.ecKey = ecKey;
        address = Address.fromKey(netParams, ecKey, scriptType);
    }
    
    /**
     * Return the signing key for an input, if available
     * @param input Transaction input data
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    public Optional<ECKey> keyForInput(TransactionInputData input) {
        return input.address()
                .filter(address::equals)
                .map(a -> ecKey);
    }

}
