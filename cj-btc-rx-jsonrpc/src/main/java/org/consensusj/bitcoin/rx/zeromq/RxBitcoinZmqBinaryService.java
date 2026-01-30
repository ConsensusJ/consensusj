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

import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.internal.BitcoinClientThreadFactory;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.Context;
import org.consensusj.bitcoin.rx.RxBlockchainBinaryService;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;
import org.reactivestreams.Publisher;

import java.io.Closeable;
import java.net.URI;
import java.util.Optional;

import static org.consensusj.bitcoin.rx.zeromq.BitcoinZmqMessage.Topic.*;

/**
 * Service to listen for ZMQ messages from multiple TCP ports. Uses Bitcoin Core JSON-RPC to find
 * the TCP address of the required services.
 * TODO: Support using all topics, notjust `rawblock` and `rawtx`.
 */
public class RxBitcoinZmqBinaryService implements RxBlockchainBinaryService, Closeable {
    protected final BitcoinClient client;
    private final RxBitcoinSinglePortZmqService blockService;
    private final RxBitcoinSinglePortZmqService txService;

    private final Flowable<byte[]> flowableRawTx;
    private final Flowable<byte[]> flowableRawBlock;

    private final BitcoinClientThreadFactory threadFactory;

    public RxBitcoinZmqBinaryService(Network network, URI rpcUri, String rpcUser, String rpcPassword) {
        this(new RxBitcoinClient(network, rpcUri, rpcUser, rpcPassword));
    }

    public RxBitcoinZmqBinaryService(BitcoinClient client) {
        this.client = client;
        threadFactory = new BitcoinClientThreadFactory(Context.getOrCreate(), "RxBitcoinZmq Thread");

        // TODO: Create background thread to look for the ZMQ Ports
        BitcoinZmqPortFinder portFinder = new BitcoinZmqPortFinder(client);

        Optional<URI> blockServiceURI = portFinder.findPort(rawblock);
        Optional<URI> txServiceURI = portFinder.findPort(rawtx);

        if (!(blockServiceURI.isPresent() && txServiceURI.isPresent())) {
            throw new RuntimeException("Bitcoin Core configuration error");
        }

        if (blockServiceURI.get().equals(txServiceURI.get())) {
            // URIs are the same we can use a single connection
            blockService = new RxBitcoinSinglePortZmqService(blockServiceURI.get(), threadFactory, rawblock, rawtx);
            txService = blockService;
        } else {
            blockService = new RxBitcoinSinglePortZmqService(blockServiceURI.get(), threadFactory, rawblock);
            txService = new RxBitcoinSinglePortZmqService(txServiceURI.get(), threadFactory, rawtx);
        }

        flowableRawBlock = Flowable.fromPublisher(blockService.blockBinaryPublisher());
        flowableRawTx = Flowable.fromPublisher(txService.transactionBinaryPublisher());
    }

    @Override
    public Publisher<byte[]> transactionBinaryPublisher() {
        return flowableRawTx;
    }

    @Override
    public Publisher<byte[]> transactionHashBinaryPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Publisher<byte[]> blockBinaryPublisher() {
        return flowableRawBlock;
    }

    @Override
    public Publisher<byte[]> blockHashBinaryPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {
        blockService.close();
        txService.close();
        client.close();
    }
}
