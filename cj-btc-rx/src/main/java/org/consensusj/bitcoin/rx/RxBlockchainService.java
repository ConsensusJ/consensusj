package org.consensusj.bitcoin.rx;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.reactivestreams.Publisher;

import java.io.Closeable;

/**
 * Reactive Streams interface for subscribing to reactive blockchain data.
 *
 * There are at least 3 possible implementations
 * 1. A bitcoinj {@link org.bitcoinj.core.PeerGroup}
 * 2. The ZeroMQ (and JSON-RPC) service of a trusted Bitcoin Core node
 * 3. A Bitcoin web service using WebSocket
 *
 * Note: Implementation instances may throw {@link UnsupportedOperationException} if they don't support a particular
 * published data type.
 */
public interface RxBlockchainService extends Closeable {
    NetworkParameters getNetworkParameters();
    Publisher<Transaction> transactionPublisher();
    Publisher<Sha256Hash> transactionHashPublisher();
    Publisher<Block> blockPublisher();
    Publisher<Sha256Hash> blockHashPublisher();
    Publisher<Integer> blockHeightPublisher();
    Publisher<ChainTip> chainTipPublisher();
}
