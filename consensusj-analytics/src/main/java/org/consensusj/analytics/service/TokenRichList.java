package org.consensusj.analytics.service;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Sha256Hash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response format for Omni rich list queries.
 * 
 * TODO: Jackson annotations?
 */
public class TokenRichList<N extends Number & Comparable<? super N>, ID> {
    private final int blockHeight;
    private final Sha256Hash blockHash;
    private final long timestamp;
    private final ID currencyID;
    private final List<TokenBalancePair<N>> richList;
    private final N otherBalanceTotal;
    
    public TokenRichList(int blockHeight,
                         Sha256Hash blockHash,
                         long timestamp,
                         ID currencyID,
                         List<TokenBalancePair<N>> richList,
                         N otherBalanceTotal) {
        this.blockHeight = blockHeight;
        this.blockHash = blockHash;
        this.timestamp = timestamp;
        this.currencyID = currencyID;
        this.richList = Collections.unmodifiableList(new ArrayList<>(richList));
        this.otherBalanceTotal = otherBalanceTotal;
    }

    public static class TokenBalancePair<N extends Number & Comparable<? super N>> {
        private final Address address;
        private final N balance;

        public TokenBalancePair(Address address, N balance) {
            this.address = address;
            this.balance = balance;
        }

        public Address getAddress() {
            return address;
        }

        public N getBalance() {
            return balance;
        }
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public Sha256Hash getBlockHash() {
        return blockHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ID getCurrencyID() {
        return currencyID;
    }

    public List<TokenBalancePair<N>> getRichList() {
        return richList;
    }

    public N getOtherBalanceTotal() {
        return otherBalanceTotal;
    }
}
