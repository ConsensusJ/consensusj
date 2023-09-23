package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.jsonrpc.AsyncSupport;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * A JSON-RPC client interface that provides ChainTipService
 */
@Deprecated
public interface RxJsonChainTipClient extends ChainTipService, RxJsonRpcClient {

    /**
     * Repeatedly once-per-new-block poll a method
     *
     * @param method A supplier (should be an RPC Method) that can throw {@link Exception}.
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    @Deprecated
    default <RSLT> Flowable<RSLT> pollOnNewBlock(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnce(method));
    }

    /**
     * Repeatedly once-per-new-block poll an async method
     *
     * @param supplier A supplier (should be an RPC Method) of a CompletionStage
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    default <RSLT> Flowable<RSLT> pollOnNewBlockAsync(Supplier<CompletionStage<RSLT>> supplier) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnceAsync(supplier));
    }
}
