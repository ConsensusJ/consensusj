package org.consensusj.bitcoin.rx.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;
import org.consensusj.jsonrpc.AsyncSupport;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;

import java.net.URI;

/**
 * A {@link BitcoinClient} enhanced with Reactive features
 * TODO: Consider making this an interface?
 */
public class RxBitcoinClient extends BitcoinClient implements ChainTipService, RxJsonRpcClient {
    ChainTipService chainTipService;

    public RxBitcoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword);
        boolean useZmq = true;
        if (useZmq) {
            chainTipService = new RxBitcoinZmqService(this);
        } else {
            chainTipService = new PollingChainTipService(this);
        }
    }

    @Override
    public Flowable<ChainTip> chainTipPublisher() {
        return Flowable.fromPublisher(chainTipService.chainTipPublisher());
    }

    /**
     * Repeatedly once-per-new-block poll a method
     *
     * @param method A supplier (should be an RPC Method) that can throw {@link Exception}.
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    <RSLT> Flowable<RSLT> pollOnNewBlock(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnce(method));
    }
}
