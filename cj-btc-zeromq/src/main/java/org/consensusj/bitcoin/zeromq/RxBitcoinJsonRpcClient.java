package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.jsonrpc.AsyncSupport;
import org.reactivestreams.Publisher;

/**
 * Reactive methods for Bitcoin JSON-RPC clients
 */
public interface RxBitcoinJsonRpcClient extends RxJsonRpcClient {

    /**
     * This method will give you a stream of ChainTips
     *
     * @return An Publisher for the sequence
     */
     Publisher<ChainTip> chainTipService();

    /**
     * Poll a method, repeatedly once-per-new-block
     *
     * @param method A supplier (should be an RPC Method) that can throw {@link Exception}.
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    default <RSLT> Publisher<RSLT> pollOnNewBlock(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Flowable.fromPublisher(chainTipService()).flatMapMaybe(tip -> pollOnce(method));
    }
}
