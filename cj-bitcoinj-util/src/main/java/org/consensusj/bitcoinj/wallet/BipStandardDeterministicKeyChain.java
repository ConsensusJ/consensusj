package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;

/**
 * Deterministic Keychain for BIP44 and related BIPs
 * <p>
 * This class adds two things to {@link DeterministicKeyChain}:
 * <ol>
 *     <li>A constructor that builds the keychain using the correct paths for BIP44, etc.</li>
 *     <li>Methods to get keys and addresses -- functionality that could/should also work with BIP 32</li>
 * </ol>
 */
public class BipStandardDeterministicKeyChain extends DeterministicKeyChain {
    private static final BipStandardKeyChainGroupStructure pathStructure = new BipStandardKeyChainGroupStructure(){};

    private final Script.ScriptType outputScriptType;
    private final NetworkParameters netParams;
    private final HDPath pathReceiving;
    private final HDPath pathChange;

    /**
     * Constructor for a BIP44-family compliant DeterministicKeyChain
     * @param seed Seed to use
     * @param netParams used to set coinType, etc
     * @param outputScriptType script type for determining the purpose child
     * @param accountIndex account index to use for the account child
     */
    public BipStandardDeterministicKeyChain(DeterministicSeed seed, Script.ScriptType outputScriptType, NetworkParameters netParams, int accountIndex) {
        super(seed, null, outputScriptType, pathStructure.pathFor(outputScriptType, netParams, accountIndex));
        this.outputScriptType = outputScriptType;
        this.netParams = netParams;
        pathReceiving = super.getAccountPath().extend(BipStandardKeyChainGroupStructure.CHANGE_RECEIVING);
        pathChange = super.getAccountPath().extend(BipStandardKeyChainGroupStructure.CHANGE_CHANGE);
    }
    
    public Address addressFromKey(ECKey key) {
        return Address.fromKey(netParams, key, outputScriptType);
    }

    public Address receivingAddr(int index) {
        HDPath indexPath = pathReceiving.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public Address changeAddr(int index) {
        HDPath indexPath = pathChange.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public Address address(HDPath indexPath) {
        DeterministicKey key = key(indexPath);
        return Address.fromKey(netParams, key, outputScriptType);
    }

    public DeterministicKey receivingKey(int index) {
        return key(pathReceiving.extend(new ChildNumber(index)));
    }

    public DeterministicKey changeKey(int index) {
        return key(pathChange.extend(new ChildNumber(index)));
    }

    public DeterministicKey key(HDPath indexPath) {
        return getKeyByPath(indexPath,true);
    }
}
