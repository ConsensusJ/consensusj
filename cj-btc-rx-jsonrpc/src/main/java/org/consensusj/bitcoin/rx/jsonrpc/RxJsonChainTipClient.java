package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.rpc.ChainTipClient;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.jsonrpc.AsyncSupport;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;

import java.util.List;
import java.util.Optional;

/**
 * A JSON-RPC client interface that provides ChainTipService
 */
public interface RxJsonChainTipClient extends ChainTipService, ChainTipClient, RxJsonRpcClient {

    /**
     * Repeatedly once-per-new-block poll a method
     *
     * @param method A supplier (should be an RPC Method) that can throw {@link Exception}.
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    default <RSLT> Flowable<RSLT> pollOnNewBlock(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnce(method));
    }

    /**
     * Get the active chain tip if there is one (useful for polling clients)
     *
     * @return The active ChainTip if available (onSuccess) otherwise onComplete (if not available) or onError (if error occurred)
     */
    default Maybe<ChainTip> currentChainTipMaybe() {
        return pollOnce(this::getChainTips)
                .mapOptional(RxJsonChainTipClient::getActiveChainTip);
    }

    private static Optional<ChainTip> getActiveChainTip(List<ChainTip> chainTips) {
        return chainTips.stream().filter(tip -> tip.getStatus().equals("active")).findFirst();
    }
}
