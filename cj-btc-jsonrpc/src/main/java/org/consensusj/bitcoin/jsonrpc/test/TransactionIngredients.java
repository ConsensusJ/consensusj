package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
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
