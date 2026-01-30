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
package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

import java.util.Arrays;
import java.util.Optional;

/**
 * A simple transaction signer using a single ECKey that can sign either
 * {@link ScriptType#P2PKH} or {@link ScriptType#P2WPKH} transactions.
 */
public class ECKeySigner implements BaseTransactionSigner {
    private final ECKey ecKey;

    /**
     * Construct a signer from a single key
     * @param ecKey signing key
     */
    public ECKeySigner(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    /**
     * Return the signing key for an input, if available
     * @param input Transaction input data
     * @return Signing key, if available, {@link Optional#empty()} otherwise.
     */
    public Optional<ECKey> keyForInput(TransactionInputData input) {
        Script s = input.script();
        return  ((ScriptPattern.isP2PKH(s) || ScriptPattern.isP2WPKH(s)) && Arrays.equals(s.getPubKeyHash(), ecKey.getPubKeyHash()))
                ? Optional.of(ecKey)
                : Optional.empty();
    }
}
