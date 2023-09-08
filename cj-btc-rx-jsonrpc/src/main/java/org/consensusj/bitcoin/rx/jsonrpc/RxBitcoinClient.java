package org.consensusj.bitcoin.rx.jsonrpc;

import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;

import javax.net.ssl.SSLContext;
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
 * to {@code cj-btc-rx?}
 */
public class RxBitcoinClient extends BitcoinExtendedClient implements RxJsonChainTipClient {
    private final ChainTipService chainTipService;

    public RxBitcoinClient(Network network, URI server, String rpcuser, String rpcpassword) {
        this(network, server, rpcuser, rpcpassword, true);
    }

    public RxBitcoinClient(Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        this(getDefaultSSLContext(), network, server, rpcuser, rpcpassword, useZmq);
    }

    public RxBitcoinClient(SSLContext sslContext, Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        super(sslContext, network, server, rpcuser, rpcpassword);
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
}
