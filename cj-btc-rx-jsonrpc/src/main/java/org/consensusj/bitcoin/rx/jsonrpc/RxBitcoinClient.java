package org.consensusj.bitcoin.rx.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;

import java.net.URI;

/**
 * A {@link BitcoinClient} enhanced with Reactive features. Can use either ZeroMQ or polling
 * to implement {@link RxJsonChainTipClient}.
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
}
