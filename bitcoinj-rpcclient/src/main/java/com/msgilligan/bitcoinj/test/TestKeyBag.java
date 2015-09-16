package com.msgilligan.bitcoinj.test;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;

import java.util.HashMap;

/**
 * Brain-dead simple KeyBag for testing
 *
 * NOTE: I can't remember what I was doing when I created this or if it even works. Should I delete it?
 *
 */
public class TestKeyBag implements KeyBag {
    private HashMap<byte[], ECKey> keys = new HashMap<byte[], ECKey>();

    @Override
    public ECKey findKeyFromPubHash(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ECKey findKeyFromPubKey(byte[] bytes) {
        return keys.get(bytes);
    }

    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public void add(ECKey key) {
        keys.put(key.getPubKey(), key);
    }
}
