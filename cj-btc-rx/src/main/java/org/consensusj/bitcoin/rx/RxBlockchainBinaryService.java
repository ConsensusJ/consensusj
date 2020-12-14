package org.consensusj.bitcoin.rx;

import org.reactivestreams.Publisher;

import java.io.Closeable;

/**
 * For lower-level, higher-performance reactive subscriptions to blockchain data where we don't
 * want the overhead of parsing (and instantiating as a tree of objects) each block or transaction.
 * (e.g. a proxy or relay server)
 *
 *  Note: Implementation instances may throw {@link UnsupportedOperationException} if they don't support a particular
 *  stream type.
 */
public interface RxBlockchainBinaryService extends Closeable {
    Publisher<byte[]> transactionBinaryPublisher();
    Publisher<byte[]> transactionHashBinaryPublisher();
    Publisher<byte[]> blockBinaryPublisher();
    Publisher<byte[]> blockHashBinaryPublisher();
}
