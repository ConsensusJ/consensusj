package org.consensusj.bitcoin.jsonrpc;

import org.bitcoinj.core.NetworkParameters;

/**
 * Has a read-only Bitcoin network parameters property.
 *
 * Ideally this property is final and set in the constructor.
 * @deprecated bitcoinj 0.17 will provide network enum types that will replace NetworkParameters
 */
@Deprecated
public interface NetworkParametersProperty {
    @Deprecated
    NetworkParameters getNetParams();
}
