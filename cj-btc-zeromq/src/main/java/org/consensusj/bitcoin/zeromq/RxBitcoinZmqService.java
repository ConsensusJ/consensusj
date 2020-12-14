package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.BlockUtil;
import org.consensusj.bitcoin.rx.RxBlockchainService;

import java.io.Closeable;
import java.net.URI;
import java.util.concurrent.CompletableFuture;


/**
 *  Add conversion to bitcoinj-types to {@link RxBitcoinZmqBinaryService}. Also
 *  uses the JSON-RPC client to fetch an initial {@link Block} so subscribers don't
 *  have to wait ~ten minutes for one.
 */
public class RxBitcoinZmqService extends RxBitcoinZmqBinaryService implements RxBlockchainService, Closeable {
    private final Context bitcoinContext;
    private final BitcoinSerializer bitcoinSerializer;
    
    public RxBitcoinZmqService(NetworkParameters networkParameters, URI rpcUri, String rpcUser, String rpcPassword) {
        super(networkParameters, rpcUri, rpcUser, rpcPassword);
        bitcoinContext = new Context(networkParameters);
        bitcoinSerializer = networkParameters.getSerializer(false);
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    @Override
    public Flowable<Transaction> transactionPublisher() {
        return transactionBinaryPublisher()
                .map(bytes -> new Transaction(networkParameters, bytes));   // Deserialize to Transaction
    }

    @Override
    public Flowable<Sha256Hash> transactionHashPublisher() {
        return transactionHashBinaryPublisher()
                .map(Sha256Hash::wrap);            // Deserialize to Sha256Hash
    }

    @Override
    public Flowable<Block> blockPublisher() {
        return blockBinaryPublisher()
                .map(bitcoinSerializer::makeBlock)  // Deserialize to bitcoinj Block
                .startWith(getLatestBlockViaRpc()); // Use JSON-RPC client to fetch an initial block
    }
    
    @Override
    public Flowable<Sha256Hash> blockHashPublisher() {
        return blockHashBinaryPublisher()
                .map(Sha256Hash::wrap);             // Deserialize to Sha256Hash
    }

    @Override
    public Flowable<Integer> blockHeightPublisher() {
        return blockPublisher()
                .map(BlockUtil::blockHeightFromCoinbase);   // Extract block height
    }

    @Override
    public void close()  {
        super.close();
    }

    /**
     * Get the latest (aka "best") {@link Block} via JSON-RPC.
     * 
     * @return A "hot" {@code Single} that will fetch the "best" block via JSON-RPC
     */
    private Single<Block> getLatestBlockViaRpc() {
        return Single.defer(() -> Single.fromCompletionStage(getBestBlock()));
    }

    private CompletableFuture<Block> getBestBlock() {
        return getBlockChainInfoAsync().thenCompose(info -> getBlockAsync(info.getBestBlockHash()));
    }

    private CompletableFuture<Block> getBlockAsync(Sha256Hash blockHash) {
        return client.supplyAsync(() -> client.getBlock(blockHash));
    }

    private CompletableFuture<BlockChainInfo> getBlockChainInfoAsync() {
        return client.supplyAsync(client::getBlockChainInfo);
    }

    /**
     * This method might be useful if we end up needing bitcoinj Context-aware threads
     * for deserializing binary messages to bitcoinj types.
     */
    private Thread createZMQThread(Runnable runnable) {
        return new Thread(() -> {
            Context.propagate(this.bitcoinContext);
            runnable.run();
        }, "ZMQ Bitcoin Thread (" + networkParameters.getId()  +")");
    }

}
