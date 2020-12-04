package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.core.Observable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 * Reactive interface to Bitcoin Core ZeroMQ messages
 */
public interface BitcoinZmqService extends AutoCloseable {
    Observable<Block> observableBlock();
    Observable<Sha256Hash> observableBlockHash();
    Observable<Transaction> observableTransaction();
    Observable<Sha256Hash> observableTransactionHash();
    // Observable<Object> rxSequences(); // TBD
}
