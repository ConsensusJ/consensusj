package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;
import org.consensusj.bitcoin.util.BlockUtil;

import java.net.URI;

/**
 * A {@link BitcoinClient} enhanced with Reactive features. Can use either ZeroMQ or polling
 * to implement {@link RxJsonChainTipClient}.
 * <p>
 * TODO: answer the below questions
 * <p>
 * Should this class eventually implement {@link org.consensusj.bitcoin.rx.RxBlockchainService}
 * or {@link org.consensusj.bitcoin.rx.RxBlockchainBinaryService}?
 * <p>
 * Should this class be renamed to {@code RxBitcoinJsonRpcClient} and the {@code RxBitcoinClient} interface be moved
 * to {@code cj-btx-rx?}
 */
public class RxBitcoinClient extends BitcoinClient implements RxJsonChainTipClient {
    ChainTipService chainTipService;

    public RxBitcoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        this(netParams, server, rpcuser, rpcpassword, true);
    }

    public RxBitcoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        super(netParams, server, rpcuser, rpcpassword);
        // TODO: Determine if ZMQ is available by querying the server
        // TODO: Determine whether server is up or down -- add a session re-establishment service
        if (useZmq) {
            chainTipService = new RxBitcoinZmqService(this);
        } else {
            chainTipService = new PollingChainTipServiceImpl(this);
        }
    }

    @Override
    public Flowable<ChainTip> chainTipPublisher() {
        return Flowable.fromPublisher(chainTipService.chainTipPublisher());
    }

    /**
     * Convert best {@link Block} to active {@link ChainTip}. If BIP34 is activated this
     * is purely computational, otherwise I/O is required to fetch the {@code ChainTip}
     *
     * @param block Input best block
     * @return active ChainTip assuming this block is the "best block"
     */
    public Single<ChainTip> activeChainTipFromBestBlock(Block block) {
        int height = BlockUtil.blockHeightFromCoinbase(block);
        if (height != -1) {
            return Single.just(ChainTip.ofActive(height, block.getHash()));
        } else {
            return getActiveChainTipViaRpc();
        }
    }

    /**
     * Get the latest (aka "best") {@link Block} via JSON-RPC.
     * <p>
     * Generally, you should use the {@link org.consensusj.bitcoin.rx.RxBlockchainService} or
     * {@link org.consensusj.bitcoin.rx.RxBlockchainBinaryService} interface if you
     * are looking for a reactive interface to blocks, because those interfaces will
     * be more efficient. This method <b>always</b> composes two unique RPC calls to the server.
     *
     * @return A "hot" {@code Single} that will fetch the "best" block via JSON-RPC
     */
    public Single<Block> getBestBlockViaRpc() {
        return getActiveChainTipViaRpc().flatMap(tip -> getBlockSingle(tip.getHash()));
    }

    private Single<ChainTip> getActiveChainTipViaRpc() {
        return call(this::getChainTips)
                .map(ChainTip::findActiveChainTipOrElseThrow);
    }

    private Single<Block> getBlockSingle(Sha256Hash blockHash) {
        return call(() -> getBlock(blockHash));
    }
}
