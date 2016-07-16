package com.msgilligan.bitcoinj.spock.tx

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.params.MainNetParams
import spock.lang.Specification


/**
 *  Constants (and later methods) that can be used for multiple transaction tests
 */
class BaseTransactionSpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final NotSoPrivatePrivateKey = new BigInteger(1, "180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19".decodeHex())
    static final utxo_id = Sha256Hash.wrap("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final utxo_amount = 0.00091234.btc
    static final fromKey = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)
    static final fromAddr = fromKey.toAddress(mainNetParams)
    static final toAddr = new Address(mainNetParams, "1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa")
}