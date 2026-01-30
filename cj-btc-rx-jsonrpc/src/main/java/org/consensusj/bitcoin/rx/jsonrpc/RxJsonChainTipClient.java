/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.Flowable;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.jsonrpc.AsyncSupport;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;

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
    default <RSLT> Publisher<RSLT> pollOnNewBlock(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnce(method));
    }

    /**
     * Repeatedly once-per-new-block poll an async method
     *
     * @param supplier A supplier (should be an RPC Method) of a CompletionStage
     * @param <RSLT> The type of the expected result
     * @return An Observable for the expected result type, so we can expect one call to {@code onNext} per block.
     */
    default <RSLT> Publisher<RSLT> pollOnNewBlockAsync(Supplier<CompletionStage<RSLT>> supplier) {
        return Flowable.fromPublisher(chainTipPublisher()).flatMapMaybe(tip -> pollOnceAsync(supplier));
    }
}
