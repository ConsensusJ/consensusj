package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import io.reactivex.rxjava3.core.Observable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.BlockUtil;
import org.consensusj.bitcoin.rx.RxBlockchainService;

import java.net.URI;
import java.util.Optional;

import static org.consensusj.bitcoin.zeromq.BitcoinZmqMessage.Topic.*;

/**
 * Service to listen for ZMQ messages from multiple TCP ports. Uses Bitcoin Core JSON-RPC to find
 * the TCP address of the required services.
 */
public class RxBitcoinZmqService implements RxBlockchainService, BitcoinZmqService, AutoCloseable {

    private final BitcoinClient client;
    private final BitcoinZmqService blockService;
    private final BitcoinZmqService txService;

    private final Observable<Transaction> observableRawTx;
    private final Observable<Block> observableRawBlock;

    public RxBitcoinZmqService(NetworkParameters networkParameters, URI rpcUri, String rpcUser, String rpcPassword) throws Exception {
        client = new BitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword);

        BitcoinZmqPortFinder portFinder = new BitcoinZmqPortFinder(client);

        Optional<URI> blockServiceURI = portFinder.findPort(rawblock);
        Optional<URI> txServiceURI = portFinder.findPort(rawtx);

        if (!(blockServiceURI.isPresent() && txServiceURI.isPresent())) {
            throw new RuntimeException("Bitcoin Core configuration error");
        }

        blockService = new RxBitcoinSinglePortZmqService(networkParameters, blockServiceURI.get());
        txService = new RxBitcoinSinglePortZmqService(networkParameters, txServiceURI.get());

        observableRawBlock = blockService.observableBlock();
        observableRawTx = txService.observableTransaction();
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return client.getNetParams();
    }

    @Override
    public Observable<Transaction> observableTransaction() {
        return observableRawTx;
    }

    @Override
    public Observable<Sha256Hash> observableTransactionHash() {
        return observableRawTx.map(Transaction::getTxId);
    }

    @Override
    public Observable<Block> observableBlock() {
        return observableRawBlock;
    }

    @Override
    public Observable<Sha256Hash> observableBlockHash() {
        return observableRawBlock.map(Block::getHash);
    }

    @Override
    public Observable<Integer> observableBlockHeight() {
        return observableRawBlock.map(BlockUtil::blockHeightFromCoinbase);
    }

    @Override
    public void close() throws Exception {
        blockService.close();
        txService.close();
    }
}
