package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.rx.RxBlockchainBinaryService;

import java.io.Closeable;
import java.net.URI;
import java.util.Optional;

import static org.consensusj.bitcoin.zeromq.BitcoinZmqMessage.Topic.*;

/**
 * Service to listen for ZMQ messages from multiple TCP ports. Uses Bitcoin Core JSON-RPC to find
 * the TCP address of the required services.
 * TODO: Support using all topics, notjust `rawblock` and `rawtx`.
 */
public class RxBitcoinZmqBinaryService implements RxBlockchainBinaryService, Closeable {

    protected final NetworkParameters networkParameters;
    protected final BitcoinClient client;
    private final RxBitcoinSinglePortZmqService blockService;
    private final RxBitcoinSinglePortZmqService txService;

    private final Flowable<byte[]> observableRawTx;
    private final Flowable<byte[]> observableRawBlock;

    public RxBitcoinZmqBinaryService(NetworkParameters networkParameters, URI rpcUri, String rpcUser, String rpcPassword) {
        this.networkParameters = networkParameters;
        client = new BitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword);

        BitcoinZmqPortFinder portFinder = new BitcoinZmqPortFinder(client);

        Optional<URI> blockServiceURI = portFinder.findPort(rawblock);
        Optional<URI> txServiceURI = portFinder.findPort(rawtx);

        if (!(blockServiceURI.isPresent() && txServiceURI.isPresent())) {
            throw new RuntimeException("Bitcoin Core configuration error");
        }

        if (blockServiceURI.get().equals(txServiceURI.get())) {
            // URIs are the same we can use a single connection
            blockService = new RxBitcoinSinglePortZmqService(blockServiceURI.get(), rawblock, rawtx);
            txService = blockService;
        } else {
            blockService = new RxBitcoinSinglePortZmqService(blockServiceURI.get(), rawblock);
            txService = new RxBitcoinSinglePortZmqService(txServiceURI.get(), rawtx);
        }

        observableRawBlock = blockService.blockBinaryPublisher();
        observableRawTx = txService.transactionBinaryPublisher();
    }

    @Override
    public Flowable<byte[]> transactionBinaryPublisher() {
        return observableRawTx;
    }

    @Override
    public Flowable<byte[]> transactionHashBinaryPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Flowable<byte[]> blockBinaryPublisher() {
        return observableRawBlock;
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
