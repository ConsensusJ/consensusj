/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoinj.wallet;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
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
    private final Network network;
    private final HDPath pathReceiving;
    private final HDPath pathChange;

    /**
     * Constructor for a BIP44-family compliant DeterministicKeyChain
     * @param seed Seed to use
     * @param network used to set coinType, etc
     * @param outputScriptType script type for determining the purpose child
     */
    public BipStandardDeterministicKeyChain(DeterministicSeed seed, ScriptType outputScriptType, Network network) {
        super(seed, null, outputScriptType, KeyChainGroupStructure.BIP43.accountPathFor(outputScriptType, network));
        this.outputScriptType = outputScriptType;
        this.network = network;
        pathReceiving = super.getAccountPath().extend(DeterministicKeyChain.EXTERNAL_SUBPATH);
        pathChange = super.getAccountPath().extend(DeterministicKeyChain.INTERNAL_SUBPATH);
    }

    /**
     * Construct a BipStandardDeterministicKeyChain from a DeterministicKeyChain
     * THIS IS NOT A WRAPPER, BUT A COPY -- THIS IS PROBABLY NOT WHAT WE WANT!
     * @param hdChain existing DeterministicKeyChain
     * @param network network (used for constructing addresses)
     */
    public BipStandardDeterministicKeyChain(DeterministicKeyChain hdChain, Network network) {
        super(Objects.requireNonNull(hdChain.getSeed()),
                null,
                hdChain.getOutputScriptType(),
                hdChain.getAccountPath());
        this.outputScriptType = hdChain.getOutputScriptType();
        this.network = network;
        pathReceiving = super.getAccountPath().extend(DeterministicKeyChain.EXTERNAL_SUBPATH);
        pathChange = super.getAccountPath().extend(DeterministicKeyChain.INTERNAL_SUBPATH);
    }

    public Address addressFromKey(ECKey key) {
        return key.toAddress(outputScriptType, network);
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
