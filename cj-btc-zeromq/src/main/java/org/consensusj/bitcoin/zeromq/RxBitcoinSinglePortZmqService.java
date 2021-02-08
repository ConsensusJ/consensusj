package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.consensusj.bitcoin.rx.RxBlockchainBinaryService;
import org.consensusj.rx.zeromq.ZmqTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bitcoin Zmq <b>binary</b> service that connects to a single port.
 * Use {@link RxBitcoinZmqBinaryService} if Bitcoin Core is configured to run ZMQ on multiple ports.
 * See: <a href="https://github.com/bitcoin/bitcoin/pull/19572">PR 19752</a> for sequence topic information
 */
public class RxBitcoinSinglePortZmqService implements RxBlockchainBinaryService, Closeable {
    private static final Logger log = LoggerFactory.getLogger(RxBitcoinSinglePortZmqService.class);

    private final URI tcpAddress;
    private final Set<BitcoinZmqMessage.Topic> topicSet;

    private final ZmqTopicPublisher zmqTopicPublisher;

    private final FlowableProcessor<byte[]> rawTxProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> rawBlockProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> hashTxProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> hashBlockProcessor = PublishProcessor.create();

    private long hashBlockSeq = -1;
    private long hashTxSeq = -1;
    private long rawBlockSeq = -1;
    private long rawTxSeq = -1;
    
    public RxBitcoinSinglePortZmqService(URI tcpAddress, BitcoinZmqMessage.Topic... topics) {
        this(tcpAddress, Set.of(topics));
    }

    private RxBitcoinSinglePortZmqService(URI tcpAddress, Collection<BitcoinZmqMessage.Topic> topics) {
        this.tcpAddress = tcpAddress;
        topicSet = Collections.unmodifiableSet(new HashSet<>(topics));
        List<String> stringTopics = topics.stream().map(BitcoinZmqMessage.Topic::toString).collect(Collectors.toList());
        zmqTopicPublisher = new ZmqTopicPublisher(tcpAddress, stringTopics);
        for (BitcoinZmqMessage.Topic topic : topics) {
            zmqTopicPublisher.topicPublisher(topic.toString())
                    .toObservable()
                    .subscribe(this::processMessage);
        }
    }


    @Override
    public Flowable<byte[]> transactionBinaryPublisher() {
        return rawTxProcessor;
    }

    @Override
    public Flowable<byte[]> transactionHashBinaryPublisher() {
        return hashTxProcessor;
    }

    @Override
    public Flowable<byte[]> blockBinaryPublisher() {
        return rawBlockProcessor;
    }

    @Override
    public Flowable<byte[]> blockHashBinaryPublisher() {
        return hashBlockProcessor;
    }


    @Override
    public void close() {
        zmqTopicPublisher.close();
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
        hashTxProcessor.onNext(dataBytes);
    }

    private void processHashBlock(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.hashblock, hashBlockSeq, seqNumber);
        hashBlockSeq = seqNumber;
        hashBlockProcessor.onNext(dataBytes);
    }

    private void processRawTx(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.rawtx, rawTxSeq, seqNumber);
        rawTxSeq = seqNumber;
        rawTxProcessor.onNext(dataBytes);
    }

    private void processRawBlock(byte[] dataBytes, long seqNumber) {
        checkSequenceNumber(BitcoinZmqMessage.Topic.rawblock, rawBlockSeq, seqNumber);
        rawBlockSeq = seqNumber;
        rawBlockProcessor.onNext(dataBytes);
    }

    // See: https://github.com/bitcoin/bitcoin/pull/19572
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
