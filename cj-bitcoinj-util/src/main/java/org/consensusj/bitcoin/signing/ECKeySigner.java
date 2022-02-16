package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.Script;

import java.util.Optional;

/**
 * A simple transaction signer using a single ECKey.
 */
public class ECKeySigner implements TransactionSigner {
    private final ECKey ecKey;
    private final Address address;

    public ECKeySigner(NetworkParameters netParams, ECKey ecKey, Script.ScriptType scriptType) {
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
