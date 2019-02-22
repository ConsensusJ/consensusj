package org.consensusj.decentralizedid.btcr;

import org.bitcoinj.core.Bech32;

/**
 * A Bech32 representation of a Bitcoin transaction output
 */
public class ExtendedTxRef {
    final Bech32.Bech32Data bech32;

    public ExtendedTxRef(String txRef) {
        bech32 = Bech32.decode(standardize(txRef));
    }

    static ExtendedTxRef of(String extTxRefString) {
        return new ExtendedTxRef(extTxRefString);
    }

    public String toString() {
        return Bech32.encode(bech32);
    }

    /**
     * Add the human readable part and remove the dashes to create a standard
     * Bech32 encoding.
     *
     * @param txRef An extended TxRef (Bech32 with no human
     * @return A standard Bech32 encoding that can be parsed by bitcoinj
     */
    static private String standardize(String txRef) {
        return "txtest1" + txRef.replace("-", "");
    }
}
