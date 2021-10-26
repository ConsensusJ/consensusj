package org.consensusj.bitcoin.rpc;

import org.bitcoinj.core.NetworkParameters;

/**
 * Has a read-only Bitcoin network parameters property.
 *
 * Ideally this property is final and set in the constructor.
 */
public interface NetworkParametersProperty {
    NetworkParameters getNetParams();
}
