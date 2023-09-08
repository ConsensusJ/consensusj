package org.consensusj.bitcoin.rx.zeromq;

import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.internal.BitcoinClientThreadFactory;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.Context;
import org.consensusj.bitcoin.rx.RxBlockchainBinaryService;
import org.consensusj.bitcoin.rx.jsonrpc.RxBitcoinClient;

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

        flowableRawBlock = blockService.blockBinaryPublisher();
        flowableRawTx = txService.transactionBinaryPublisher();
    }

    @Override
    public Flowable<byte[]> transactionBinaryPublisher() {
        return flowableRawTx;
    }

    @Override
    public Flowable<byte[]> transactionHashBinaryPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Flowable<byte[]> blockBinaryPublisher() {
        return flowableRawBlock;
    }

    @Override
    public Flowable<byte[]> blockHashBinaryPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close()  {
        blockService.close();
        txService.close();
        try {
            client.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
