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
package org.consensusj.bitcoinj.spock.tx

import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.crypto.ECKey
import spock.lang.Specification


/**
 *  Constants (and later methods) that can be used for multiple transaction tests
 */
abstract class BaseTransactionSpec extends Specification {
    static final NotSoPrivatePrivateKey = new BigInteger(1, "180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19".decodeHex())
    static final utxo_id = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final utxo_amount = 0.00091234.btc
    static final fromKey = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)
    static final fromAddr = fromKey.toAddress(ScriptType.P2PKH, BitcoinNetwork.MAINNET)
    static final toAddr = AddressParser.getDefault(BitcoinNetwork.MAINNET).parseAddress("1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa")
}