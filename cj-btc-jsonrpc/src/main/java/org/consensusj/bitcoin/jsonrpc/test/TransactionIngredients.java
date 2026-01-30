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
package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Address;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.TransactionOutPoint;

import java.util.List;

/**
 * Everything needed to build your own custom transaction
 * TODO: Make this an immutable bean
 */
public class TransactionIngredients {
    public Address address;
    public ECKey   privateKey;
    public List<TransactionOutPoint> outPoints;
}
