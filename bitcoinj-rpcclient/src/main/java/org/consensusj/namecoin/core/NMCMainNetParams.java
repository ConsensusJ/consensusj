package org.consensusj.namecoin.core;

import org.bitcoinj.params.MainNetParams;

/**
 * Bare minimum Namecoin support for creating Addresses on Namecoin MainNet.
 *
 */
public class NMCMainNetParams extends MainNetParams {

    public NMCMainNetParams() {
        super();
        addressHeader = 52;
        p2shHeader = 5;  // What should this be for Namecoin??
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;
    }

    private static NMCMainNetParams instance;
    public static synchronized NMCMainNetParams get() {
        if (instance == null) {
            instance = new NMCMainNetParams();
        }
        return instance;
    }

}
