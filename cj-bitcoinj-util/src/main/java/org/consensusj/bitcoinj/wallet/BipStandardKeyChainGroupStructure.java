package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroupStructure;

/**
 * KeyChainGroupStructure that supports BIP44, BIP84, etc. This should be part of bitcoinj.
 */
public interface BipStandardKeyChainGroupStructure extends KeyChainGroupStructure {
    ChildNumber PURPOSE_BIP44 = new ChildNumber(44, true);  // P2PKH
    ChildNumber PURPOSE_BIP49 = new ChildNumber(49, true);  // P2WPKH-nested-in-P2SH
    ChildNumber PURPOSE_BIP84 = new ChildNumber(84, true);  // P2WPKH

    ChildNumber COINTYPE_BTC = new ChildNumber(0, true);
    ChildNumber COINTYPE_TBTC = new ChildNumber(1, true);
    ChildNumber COINTYPE_LTC = new ChildNumber(2, true);

    ChildNumber CHANGE_RECEIVING = new ChildNumber(0, false);
    ChildNumber CHANGE_CHANGE = new ChildNumber(1, false);

    HDPath BIP44_PARENT = HDPath.m(PURPOSE_BIP44);
    HDPath BIP84_PARENT = HDPath.m(PURPOSE_BIP84);

    /** Map desired output script type to an account path */
    @Override
    default HDPath accountPathFor(Script.ScriptType outputScriptType) {
        return pathFor(outputScriptType, MainNetParams.get(), 0);
    }

    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param netId network/coin type
     * @param accountIndex account index
     * @return The HD Path: purpose / coinType / accountIndex
     */
    default HDPath pathFor(Script.ScriptType outputScriptType, String netId, int accountIndex) {
        return purpose(outputScriptType)
                .extend(coinType(netId), account(accountIndex));
    }

    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param networkParameters network/coin type
     * @param accountIndex account index
     * @return The HD Path: purpose / coinType / accountIndex
     */
    default HDPath pathFor(Script.ScriptType outputScriptType, NetworkParameters networkParameters, int accountIndex) {
        return pathFor(outputScriptType, networkParameters.getId(), accountIndex);
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