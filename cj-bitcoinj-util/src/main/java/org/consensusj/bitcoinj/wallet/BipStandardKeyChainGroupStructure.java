package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.script.Script;

/**
 * Path builder that supports BIP44, BIP84, etc. This functionality will be in the next release of bitcoinj.
 * This interface will be deprecated after the next release of bitcoinj.
 */
public interface BipStandardKeyChainGroupStructure {
    ChildNumber PURPOSE_BIP44 = new ChildNumber(44, true);  // P2PKH
    ChildNumber PURPOSE_BIP49 = new ChildNumber(49, true);  // P2WPKH-nested-in-P2SH
    ChildNumber PURPOSE_BIP84 = new ChildNumber(84, true);  // P2WPKH

    ChildNumber COINTYPE_BTC = new ChildNumber(0, true);
    ChildNumber COINTYPE_TBTC = new ChildNumber(1, true);

    ChildNumber CHANGE_RECEIVING = new ChildNumber(0, false);
    ChildNumber CHANGE_CHANGE = new ChildNumber(1, false);

    HDPath BIP44_PARENT = HDPath.m(PURPOSE_BIP44);
    HDPath BIP84_PARENT = HDPath.m(PURPOSE_BIP84);


    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param netId network/coin type
     * @return The HD Path: purpose / coinType / accountIndex
     */
    static HDPath pathFor(Script.ScriptType outputScriptType, String netId) {
        return purpose(outputScriptType)
                .extend(coinType(netId), account(0));
    }

    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param networkParameters network/coin type
     * @return The HD Path: purpose / coinType / accountIndex
     */
    static HDPath pathFor(Script.ScriptType outputScriptType, NetworkParameters networkParameters) {
        return pathFor(outputScriptType, networkParameters.getId());
    }

    /**
     * Return the (root) path containing "purpose" for the specified scriptType
     * @param scriptType script/address type
     * @return An HDPath with a BIP44 "purpose" entry
     */
    static HDPath purpose(Script.ScriptType scriptType) {
        if (scriptType == null || scriptType == Script.ScriptType.P2PKH) {
            return BIP44_PARENT;
        } else if (scriptType == Script.ScriptType.P2WPKH) {
            return BIP84_PARENT;
        } else {
            throw new IllegalArgumentException(scriptType.toString());
        }
    }

    /**
     * Return coin type path component for a network id
     * @param networkId network id string, eg. {@link NetworkParameters#ID_MAINNET}
     */
    static ChildNumber coinType(String networkId) {
        switch (networkId) {
            case NetworkParameters.ID_MAINNET:
                return COINTYPE_BTC;
            case NetworkParameters.ID_TESTNET:
                return COINTYPE_TBTC;
            case NetworkParameters.ID_REGTEST:
                return COINTYPE_TBTC;
            default:
                throw new IllegalArgumentException("Unknown network id (coin type)");
        }
    }

    /**
     * Return path component for an account
     * @param accountIndex account index
     * @return A hardened path component
     */
    static ChildNumber account(int accountIndex) {
        return new ChildNumber(accountIndex, true);
    }
}