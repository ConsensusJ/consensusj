package org.consensusj.bitcoin.rx;

import io.reactivex.rxjava3.core.Flowable;

import java.io.Closeable;

/**
 * For lower-level, higher-performance reactive subscriptions to blockchain data where we don't
 * want the overhead of parsing (and instantiating as a tree of objects) each block or transaction.
 * (e.g. a proxy or relay server)
 */
public interface RxBlockchainBinaryService extends Closeable {
    Flowable<byte[]> transactionBinaryPublisher();
    Flowable<byte[]> transactionHashBinaryPublisher();
    Flowable<byte[]> blockBinaryPublisher();
    Flowable<byte[]> blockHashBinaryPublisher();
}
