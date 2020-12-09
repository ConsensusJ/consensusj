package org.consensusj.bitcoin.rx;

import io.reactivex.rxjava3.core.Observable;

/**
 * For lower-level, higher-performance reactive subscriptions to blockchain data where we don't
 * want the overhead of parsing (and instantiating as a tree of objects) each block or transaction.
 * (e.g. a proxy or relay server)
 */
public interface RxBlockchainBytesService extends AutoCloseable {
    Observable<byte[]> observableTransactionBytes();
    Observable<byte[]> observableTransactionHashBytes();
    Observable<byte[]> observableBlockBytes();
    Observable<byte[]> observableBlockHashBytes();
}
