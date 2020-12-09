package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.rx.RxBlockchainBytesService;
import org.consensusj.bitcoin.rx.RxBlockchainService;
import org.consensusj.bitcoin.rx.BlockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BitcoinZmqService that connects to a single port. Use {@link RxBitcoinZmqService} if Bitcoin Core is configured
 * to run ZMQ on multiple ports.
 */
public class RxBitcoinSinglePortZmqService implements RxBlockchainService, RxBlockchainBytesService, BitcoinZmqService, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RxBitcoinSinglePortZmqService.class);

    private final Context bitcoinContext;
    private final NetworkParameters netParams;
    private final URI tcpAddress;
    private final Set<BitcoinZmqMessage.Topic> topicSet;
    private final BitcoinSerializer bitcoinSerializer;

    private final ZmqSubscriber zmqSubscriber;

    private final PublishProcessor<byte[]> rawTxProcessor = PublishProcessor.create();
    private final PublishProcessor<byte[]> rawBlockProcessor = PublishProcessor.create();
    private final PublishProcessor<byte[]> hashTxProcessor = PublishProcessor.create();
    private final PublishProcessor<byte[]> hashBlockProcessor = PublishProcessor.create();

    private long hashBlockSeq = -1;
    private long hashTxSeq = -1;
    private long rawBlockSeq = -1;
    private long rawTxSeq = -1;
    
    public RxBitcoinSinglePortZmqService(NetworkParameters netParams, URI tcpAddress, BitcoinZmqMessage.Topic... topics) {
        this(netParams, tcpAddress, Set.of(topics));
    }

    private RxBitcoinSinglePortZmqService(NetworkParameters netParams, URI tcpAddress, Collection<BitcoinZmqMessage.Topic> topics) {
        this.netParams = netParams;
        this.tcpAddress = tcpAddress;
        bitcoinContext = new Context(netParams);
        bitcoinSerializer = netParams.getSerializer(false);
        topicSet = Collections.unmodifiableSet(new HashSet<>(topics));
        List<String> stringTopics = topics.stream().map(BitcoinZmqMessage.Topic::toString).collect(Collectors.toList());
        zmqSubscriber = new ZmqSubscriber(tcpAddress, stringTopics, this::createZMQThread);
        for (BitcoinZmqMessage.Topic topic : topics) {
            zmqSubscriber.observableTopic(topic.toString())
                    .subscribe(this::processMessage);
        }
    }

    private Thread createZMQThread(Runnable runnable) {
        return new Thread(() -> {
            Context.propagate(this.bitcoinContext);
            runnable.run();
        }, "ZMQ Bitcoin Thread (" + this.netParams.getId()  +")");
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return netParams;
    }

    @Override
    public Observable<byte[]> observableTransactionBytes() {
        return rawTxProcessor
                .toObservable();
    }

    @Override
    public Observable<byte[]> observableTransactionHashBytes() {
        return hashTxProcessor
                .toObservable();
    }

    @Override
    public Observable<byte[]> observableBlockBytes() {
        return rawBlockProcessor
                .toObservable();
    }

    @Override
    public Observable<byte[]> observableBlockHashBytes() {
        return hashBlockProcessor
                .toObservable();
    }

    @Override
    public Observable<Block> observableBlock() {
        return rawBlockProcessor
                .map(bitcoinSerializer::makeBlock)
                .toObservable();
    }

    @Override
    public Observable<Sha256Hash> observableBlockHash() {
        return hashBlockProcessor
                .map(Sha256Hash::wrap)
                .toObservable();
    }

    @Override
    public Observable<Integer> observableBlockHeight() {
        return rawBlockProcessor
                .map(bitcoinSerializer::makeBlock)
                .map(BlockUtil::blockHeightFromCoinbase)
                .toObservable();
    }

    @Override
    public Observable<Transaction> observableTransaction() {
        return rawTxProcessor
                .map(bytes -> new Transaction(netParams, bytes))
                .toObservable();
    }

    @Override
    public Observable<Sha256Hash> observableTransactionHash() {
        return hashTxProcessor
                .map(Sha256Hash::wrap)
                .toObservable();
    }

    @Override
    public void close() {
        zmqSubscriber.close();
    }

    private void processMessage(ZMsg message) {
        log.debug("New message received.");

        if (message.size() >= 2) {
            BitcoinZmqMessage.Topic topic = BitcoinZmqMessage.Topic.valueOf(new String(message.remove().getData()));
            log.debug("Received {} message.", topic);
            byte[] dataBytes = message.remove().getData();
            long seqNumber = 0;
            if (topic != BitcoinZmqMessage.Topic.sequence)
            {
                seqNumber = fromByteArray(message.remove().getData());
                log.trace("sequence number {}:{}", topic, seqNumber);
            }
            switch (topic) {
                case hashtx: processHashTx(dataBytes, seqNumber); break;
                case hashblock: processHashBlock(dataBytes, seqNumber); break;
                case rawtx: processRawTx(dataBytes, seqNumber); break;
                case rawblock: processRawBlock(dataBytes, seqNumber); break;
                case sequence: processSequence(message); break; // Ignore for now
                default:
                    log.warn("Unknown topic: {}", topic);
            }
        } else {
            log.warn("Ignoring message with less than 3 frames");
        }
    }


    private void processHashTx(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.hashtx, hashTxSeq, seqNumber);
        hashTxSeq = seqNumber;
//        Sha256Hash hash = Sha256Hash.wrap(dataBytes);
//        log.debug("TXID: " + hash);
        hashTxProcessor.onNext(dataBytes);
    }

    private void processHashBlock(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.hashblock, hashBlockSeq, seqNumber);
        hashBlockSeq = seqNumber;
//        Sha256Hash hash = Sha256Hash.wrap(dataBytes);
//        log.debug("Block Hash: " + hash);
        hashBlockProcessor.onNext(dataBytes);
    }

    private void processRawTx(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.rawtx, rawTxSeq, seqNumber);
        rawTxSeq = seqNumber;
//        Transaction tx = new Transaction(netParams, dataBytes);
//        log.debug("Transaction: output: {}, ID: {}", tx.getOutputSum().toFriendlyString(), tx.getTxId());
        rawTxProcessor.onNext(dataBytes);
    }

    private void processRawBlock(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.rawblock, rawBlockSeq, seqNumber);
        rawBlockSeq = seqNumber;
//        Block block = bitcoinSerializer.makeBlock(dataBytes);
//        log.debug("Block: Hash: {}, # Transactions: {} ", block.getHashAsString(), block.getTransactions().size());
        rawBlockProcessor.onNext(dataBytes);
    }

    private static void processSequence(ZMsg message) {
        //checkSequenceNumber(Topic.sequence, sequenceSeq, seqNumber);
        log.warn("Got sequence: {}", message);
    }

    private static void checkSequenceNumber(BitcoinZmqMessage.Topic topic, long previous, long current) {
        if ((previous != -1) && (current != previous + 1)) {
            log.warn("Topic {} missing {} sequence numbers: previous = {}, current = {}", topic, current - (previous + 1), previous, current);
        }
    }

    private static long fromByteArray(byte[] bytes) {
        long result = 0;

        for (int i = 0; i < bytes.length; i++) {
            long ubyte = bytes[i] & 0xff;
            result += ubyte << (8 * i);
        }
        return result;
    }

}
