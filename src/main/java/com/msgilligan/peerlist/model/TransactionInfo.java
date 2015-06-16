package com.msgilligan.peerlist.model;

import org.bitcoinj.core.Transaction;

/**
 * TransactionInfo
 * <p>
 * Simple Bean Constructed from Peer class for serialization over STOMP
 */
@Deprecated
public class TransactionInfo {
    private String hash;

    public TransactionInfo(Transaction tx) {
        this.hash = tx.getHashAsString();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
