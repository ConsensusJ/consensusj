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
package org.consensusj.bitcoin.rx.zeromq;

import io.reactivex.rxjava3.core.Single;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.rx.ChainTipPublisher;
import org.consensusj.bitcoin.rx.ChainTipService;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;
import org.consensusj.bitcoin.rx.RxBlockchainService;
import org.consensusj.bitcoinj.util.BlockUtil;
import org.consensusj.rx.jsonrpc.RxJsonRpcClient;
import org.reactivestreams.Publisher;

import java.io.Closeable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 *  Add conversion to bitcoinj-types to {@code RxBitcoinZmqBinaryService}. Also
 *  uses the JSON-RPC client to fetch an initial {@link Block} so subscribers don't
 *  have to wait ~ten minutes for one.
 */
public class RxBitcoinZmqService extends RxBitcoinZmqBinaryService implements RxBlockchainService, ChainTipService, Closeable {

    private final FlowableProcessor<ChainTip> flowableChainTip = BehaviorProcessor.create();
    private final Disposable blockSubscription;

    public RxBitcoinZmqService(Network network, URI rpcUri, String rpcUser, String rpcPassword) {
        this(new RxBitcoinClient(network, rpcUri, rpcUser, rpcPassword));
    }

    public RxBitcoinZmqService(BitcoinClient client) {
        super(client);
        blockSubscription = Flowable.fromPublisher(blockPublisher())
                .flatMap(this::activeChainTipFromBestBlockPublisher)
                .distinctUntilChanged(ChainTip::getHash)
                .subscribe(this::onNextChainTip, flowableChainTip::onError, flowableChainTip::onComplete);
    }

    @Override
    public Publisher<Transaction> transactionPublisher() {
        return Flowable.fromPublisher(transactionBinaryPublisher())
                .map(bytes -> Transaction.read(ByteBuffer.wrap(bytes)));   // Deserialize to Transaction
    }

    @Override
    public Publisher<Sha256Hash> transactionHashPublisher() {
        return Flowable.fromPublisher(transactionHashBinaryPublisher())
                .map(Sha256Hash::wrap);            // Deserialize to Sha256Hash
    }

    @Override
    public Publisher<Block> blockPublisher() {
        return Flowable.fromPublisher(blockBinaryPublisher())
                .map(ByteBuffer::wrap)
                .map(Block::read)  // Deserialize to bitcoinj Block
                .startWith(defer(client::getBestBlock)); // Use JSON-RPC client to fetch an initial block
    }
    
    @Override
    public Publisher<Sha256Hash> blockHashPublisher() {
        return Flowable.fromPublisher(blockHashBinaryPublisher())
                .map(Sha256Hash::wrap);             // Deserialize to Sha256Hash
    }

    @Override
    public Publisher<Integer> blockHeightPublisher() {
        return Flowable.fromPublisher(chainTipPublisher())
                .map(ChainTip::getHeight);          // Extract block height
    }

    @Override
    public ChainTipPublisher chainTipPublisher() {
        return ChainTipPublisher.of(flowableChainTip);
    }

    @Override
    public void close()  {
        super.close();
        blockSubscription.dispose();
    }

    /**
     * Return a <i>cold</i> {@link Single} for calling a provided <b>asynchronous</b> JSON-RPC method.
     * (Uses a supplier to make sure the async call isn't made until subscription time)
     * <p>
     *  A  <i>cold</i> stream does not begin processing until someone subscribes to it.
     * @param supplier of completable
     * @param <RSLT> The type of the expected result
     * @return A <i>cold</i> {@link Single} for calling the method.
     */
    private <RSLT> Single<RSLT> defer(Supplier<CompletionStage<RSLT>> supplier) {
        return Single.defer(() -> Single.fromCompletionStage(supplier.get()));
    }

    private Publisher<ChainTip> activeChainTipFromBestBlockPublisher(Block block) {
        return Flowable.defer(() -> Flowable.fromCompletionStage(activeChainTipFromBestBlock(block)));
    }

    /**
     * Convert best {@link Block} to active {@link ChainTip}. If BIP34 is activated this
     * is purely computational, otherwise I/O is required to fetch the {@code ChainTip}
     *
     * @param block Input best block
     * @return active ChainTip assuming parameter block is the "best block"
     */
    private CompletableFuture<ChainTip> activeChainTipFromBestBlock(Block block) {
        int height = BlockUtil.blockHeightFromCoinbase(block);
        if (height != -1) {
            return CompletableFuture.completedFuture(ChainTip.ofActive(height, block.getHash()));
        } else {
            return client.getChainTipsAsync()
                    .thenApply(ChainTip::findActiveChainTipOrElseThrow);
        }
    }

    // For setting breakpoints
    void onNextChainTip(ChainTip tip) {
        flowableChainTip.onNext(tip);
    }
}
