package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.ChildNumber;

import static org.bitcoinj.base.ScriptType.P2PKH;
import static org.bitcoinj.base.ScriptType.P2WPKH;

/**
 * Path builder that supports BIP44, BIP84, etc. This functionality will be in the next release of bitcoinj.
 * This interface will be deprecated after the next release of bitcoinj.
 * @deprecated Use {@link org.bitcoinj.wallet.KeyChainGroupStructure#BIP43} and constants in {@link org.bitcoinj.wallet.DeterministicKeyChain}
 */
@Deprecated
public interface BipStandardKeyChainGroupStructure {
    ChildNumber PURPOSE_BIP44 = ChildNumber.PURPOSE_BIP44;  // P2PKH
    ChildNumber PURPOSE_BIP49 = ChildNumber.PURPOSE_BIP49;  // P2WPKH-nested-in-P2SH
    ChildNumber PURPOSE_BIP84 = ChildNumber.PURPOSE_BIP84;  // P2WPKH

    ChildNumber COINTYPE_BTC = ChildNumber.COINTYPE_BTC;
    ChildNumber COINTYPE_TBTC = new ChildNumber(1, true);

    ChildNumber CHANGE_RECEIVING = ChildNumber.ZERO;
    ChildNumber CHANGE_CHANGE = ChildNumber.ONE;

    HDPath BIP44_PARENT = HDPath.BIP44_PARENT;
    HDPath BIP84_PARENT = HDPath.BIP84_PARENT;


    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param netId network/coin type
     * @return The HD Path: purpose / coinType / accountIndex
     */
    static HDPath pathFor(org.bitcoinj.base.ScriptType outputScriptType, String netId) {
        return purpose(outputScriptType)
                .extend(coinType(netId), account(0));
    }

    /**
     * Map desired output script type and account index to an account path
     * @param outputScriptType output script type (purpose)
     * @param networkParameters network/coin type
     * @return The HD Path: purpose / coinType / accountIndex
     */
    static HDPath pathFor(org.bitcoinj.base.ScriptType outputScriptType, NetworkParameters networkParameters) {
        return pathFor(outputScriptType, networkParameters.getId());
    }

    /**
     * Return the (root) path containing "purpose" for the specified scriptType
     * @param scriptType script/address type
     * @return An HDPath with a BIP44 "purpose" entry
     */
    static HDPath purpose(org.bitcoinj.base.ScriptType scriptType) {
        if (scriptType == null || scriptType == P2PKH) {
            return BIP44_PARENT;
        } else if (scriptType == P2WPKH) {
            return BIP84_PARENT;
        } else {
            throw new IllegalArgumentException(scriptType.toString());
        }
    }

    /**
     * Return coin type path component for a network id
     * @param networkId network id string, eg. {@link BitcoinNetwork#ID_MAINNET}
     */
    static ChildNumber coinType(String networkId) {
        BitcoinNetwork network = BitcoinNetwork.fromIdString(networkId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown network id (coin type)"));
        switch (network) {
            case MAINNET:
                return COINTYPE_BTC;
            case TESTNET:
                return COINTYPE_TBTC;
            case SIGNET:
                return COINTYPE_TBTC;
            case REGTEST:
            default:
                return COINTYPE_TBTC;
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