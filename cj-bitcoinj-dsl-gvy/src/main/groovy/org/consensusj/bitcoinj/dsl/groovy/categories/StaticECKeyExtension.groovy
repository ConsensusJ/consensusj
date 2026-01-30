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
package org.consensusj.bitcoinj.dsl.groovy.categories

import org.bitcoinj.base.Base58
import org.bitcoinj.crypto.ECKey

/**
 * Extension to add static methods to ECKey
 */
class StaticECKeyExtension {
//    Don't provide this method until we know the best default for `compressed`
//    static ECKey fromWIF(ECKey self, String wifString) {
//        // Decode Base58
//        byte[] wifRaw = Base58.decodeChecked(wifString)
//        // Remove header (first byte) and checksum (4 bytes after byte 33)
//        byte[] privKey = Arrays.copyOfRange(wifRaw, 1, 33)
//        return new ECKey().fromPrivate(privKey, false)
//    }

    static ECKey fromWIF(ECKey self, String wifString, boolean compressed) {
        // Decode Base58
        byte[] wifRaw = Base58.decodeChecked(wifString)
        // Remove header (first byte) and checksum (4 bytes after byte 33)
        byte[] privKey = Arrays.copyOfRange(wifRaw, 1, 33)
        return new ECKey().fromPrivate(privKey, compressed)
    }
}
