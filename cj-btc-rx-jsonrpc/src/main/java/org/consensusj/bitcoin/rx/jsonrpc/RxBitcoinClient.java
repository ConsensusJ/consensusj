package org.consensusj.bitcoin.rx.jsonrpc;

import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.zeromq.RxBitcoinZmqService;
import org.consensusj.jsonrpc.JsonRpcTransport;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A {@link BitcoinClient} enhanced with Reactive features. Can use either ZeroMQ or polling
 * to implement {@link ChainTipService}.
 * <p>
 * TODO: answer the below questions
 * <p>
 * Should this class eventually implement {@link org.consensusj.bitcoin.rx.RxBlockchainService}
 * or {@link org.consensusj.bitcoin.rx.RxBlockchainBinaryService}?
 * <p>
 * Should this class be renamed to {@code RxBitcoinJsonRpcClient} and the {@code RxBitcoinClient} interface be moved
 * to {@code cj-btc-rx?}
 */
public class RxBitcoinClient extends BitcoinExtendedClient implements ChainTipService, RxJsonRpcClient {
    private final boolean useZmq;
    private /* Lazy */ ChainTipService chainTipService;

    public RxBitcoinClient(Network network, URI server, String rpcuser, String rpcpassword) {
        this(network, server, rpcuser, rpcpassword, true);
    }

    public RxBitcoinClient(Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        this(JsonRpcTransport.getDefaultSSLContext(), network, server, rpcuser, rpcpassword, useZmq);
    }

    public RxBitcoinClient(SSLContext sslContext, Network network, URI server, String rpcuser, String rpcpassword, boolean useZmq) {
        super(sslContext, network, server, rpcuser, rpcpassword);
        // TODO: Determine if ZMQ is available by querying the server
        this.useZmq = useZmq;
        // TODO: Determine whether server is up or down -- add a session re-establishment service
    }

    private void initChainTipService(Duration timeout) {
        if (chainTipService == null) {
            this.waitForConnected().orTimeout(timeout.toSeconds(), TimeUnit.SECONDS).join();
            if (useZmq) {
                chainTipService = new RxBitcoinZmqService(this);
            } else {
                chainTipService = new PollingChainTipServiceImpl(this);
            }
        }
    }

    /**
     * Repeatedly once-per-new-block poll an async method
     *
     * @param supplier A supplier (should be an RPC Method) of a CompletionStage
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    public <RSLT> Publisher<RSLT> pollOnNewBlockAsync(Supplier<CompletionStage<RSLT>> supplier) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMap(tip -> pollOnceAsPublisher(supplier));
    }

    /**
     * The BitcoinClient must have "connected once" before this is called. This means something else
     * needs to have called something to do that.
     * @return a publisher of Chain Tips
     */
    @Override
    public Publisher<ChainTip> chainTipPublisher() {
        initChainTipService(Duration.ofMinutes(60));
        return Flowable.fromPublisher(chainTipService.chainTipPublisher());
    }
}
