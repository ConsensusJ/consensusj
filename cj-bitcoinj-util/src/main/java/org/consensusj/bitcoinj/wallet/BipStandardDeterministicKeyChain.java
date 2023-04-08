package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroupStructure;

import java.util.Objects;

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
    private final ScriptType outputScriptType;
    private final NetworkParameters netParams;
    private final HDPath pathReceiving;
    private final HDPath pathChange;

    /**
     * Constructor for a BIP44-family compliant DeterministicKeyChain
     * @param seed Seed to use
     * @param netParams used to set coinType, etc
     * @param outputScriptType script type for determining the purpose child
     */
    public BipStandardDeterministicKeyChain(DeterministicSeed seed, ScriptType outputScriptType, NetworkParameters netParams) {
        super(seed, null, outputScriptType, KeyChainGroupStructure.BIP43.accountPathFor(outputScriptType, netParams));
        this.outputScriptType = outputScriptType;
        this.netParams = netParams;
        pathReceiving = super.getAccountPath().extend(DeterministicKeyChain.EXTERNAL_SUBPATH);
        pathChange = super.getAccountPath().extend(DeterministicKeyChain.INTERNAL_SUBPATH);
    }

    /**
     * Construct a BipStandardDeterministicKeyChain from a DeterministicKeyChain
     * @param hdChain existing DeterministicKeyChain
     * @param netParams network (used for constructing addresses)
     */
    public BipStandardDeterministicKeyChain(DeterministicKeyChain hdChain, NetworkParameters netParams) {
        super(Objects.requireNonNull(hdChain.getSeed()),
                null,
                hdChain.getOutputScriptType(),
                hdChain.getAccountPath());
        this.outputScriptType = hdChain.getOutputScriptType();
        this.netParams = netParams;
        pathReceiving = super.getAccountPath().extend(DeterministicKeyChain.EXTERNAL_SUBPATH);
        pathChange = super.getAccountPath().extend(DeterministicKeyChain.INTERNAL_SUBPATH);
    }
    
    public Address addressFromKey(ECKey key) {
        return key.toAddress(outputScriptType, netParams.network());
    }

    public Address receivingAddr(int index) {
        HDPath indexPath = pathReceiving.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public Address changeAddr(int index) {
        HDPath indexPath = pathChange.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public Address address(HDPath path) {
        DeterministicKey key = key(path);
        return addressFromKey(key);
    }

    public DeterministicKey receivingKey(int index) {
        return key(pathReceiving.extend(new ChildNumber(index)));
    }

    public DeterministicKey changeKey(int index) {
        return key(pathChange.extend(new ChildNumber(index)));
    }

    public DeterministicKey key(HDPath path) {
        return getKeyByPath(path,true);
    }
}
