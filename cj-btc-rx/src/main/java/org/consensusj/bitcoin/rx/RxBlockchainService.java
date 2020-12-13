package org.consensusj.bitcoin.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.io.Closeable;

/**
 * RxJava 3 interface for subscribing to reactive blockchain data.
 *
 * There are at least 3 possible implementations
 * 1. A bitcoinj {@link org.bitcoinj.core.PeerGroup}
 * 2. The ZeroMQ service of a  trusted Bitcoin Core node
 * 3. A Bitcoin web service using WebSocket
 *
 * Note: Implementations may throw {@link UnsupportedOperationException} if they don't support a particular
 * observable.
 */
public interface RxBlockchainService extends Closeable {
    NetworkParameters getNetworkParameters();
    Flowable<Transaction> transactionPublisher();
    Flowable<Sha256Hash> transactionHashPublisher();
    Flowable<Block> blockPublisher();
    Flowable<Sha256Hash> blockHashPublisher();
    Flowable<Integer> blockHeightPublisher();
}
