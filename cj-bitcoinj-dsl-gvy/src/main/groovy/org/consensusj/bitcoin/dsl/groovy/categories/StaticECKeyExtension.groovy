package org.consensusj.bitcoin.dsl.groovy.categories

import org.bitcoinj.core.Base58
import org.bitcoinj.core.ECKey

/**
 * Extension to add static methods to ECKey
 */
class StaticECKeyExtension {
    static ECKey fromWIF(ECKey self, String wifString) {
        // Decode Base58
        byte[] wifRaw = Base58.decodeChecked(wifString)
        // Remove header (first byte) and checksum (4 bytes after byte 33)
        byte[] privKey = Arrays.copyOfRange(wifRaw, 1, 33)
        return new ECKey().fromPrivate(privKey, false)
    }
}
