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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.consensusj.bitcoin.rx.RxBlockchainBinaryService;
import org.consensusj.rx.zeromq.RxZmqContext;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Bitcoin Zmq <b>binary</b> service that connects to a single port.
 * Use {@code RxBitcoinZmqBinaryService} if Bitcoin Core is configured to run ZMQ on multiple ports.
 * See: <a href="https://github.com/bitcoin/bitcoin/pull/19572">PR 19752</a> for sequence topic information
 */
public class RxBitcoinSinglePortZmqService implements RxBlockchainBinaryService, Closeable {
    private static final Logger log = LoggerFactory.getLogger(RxBitcoinSinglePortZmqService.class);

    private final URI tcpAddress;
    private final Set<BitcoinZmqMessage.Topic> topicSet;

    private final RxZmqContext zmqContext;

    private final FlowableProcessor<byte[]> rawTxProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> rawBlockProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> hashTxProcessor = PublishProcessor.create();
    private final FlowableProcessor<byte[]> hashBlockProcessor = PublishProcessor.create();

    private long hashBlockSeq = -1;
    private long hashTxSeq = -1;
    private long rawBlockSeq = -1;
    private long rawTxSeq = -1;


    public RxBitcoinSinglePortZmqService(URI tcpAddress, ThreadFactory threadFactory, BitcoinZmqMessage.Topic... topics) {
        this(tcpAddress, threadFactory, Set.of(topics));
    }

    private RxBitcoinSinglePortZmqService(URI tcpAddress, ThreadFactory threadFactory, Set<BitcoinZmqMessage.Topic> topics) {
        this.tcpAddress = tcpAddress;
        topicSet = Collections.unmodifiableSet(topics);
        List<String> stringTopics = topics.stream().map(BitcoinZmqMessage.Topic::toString).collect(Collectors.toList());
        zmqContext = new RxZmqContext(tcpAddress, stringTopics, threadFactory);
        for (BitcoinZmqMessage.Topic topic : topics) {
            // Subscribe to each topic
            switch (topic) {
                case hashblock:
                    getTopicPublisher(topic)
                            .subscribe(this::onNextHashBlock, hashBlockProcessor::onError, hashBlockProcessor::onComplete);
                    break;
                case hashtx:
                    getTopicPublisher(topic)
                            .subscribe(this::onNextHashTx, hashTxProcessor::onError, hashTxProcessor::onComplete);
                    break;
                case rawblock:
                    getTopicPublisher(topic)
                            .subscribe(this::onNextRawBlock, rawBlockProcessor::onError, rawBlockProcessor::onComplete);
                    break;
                case rawtx:
                    getTopicPublisher(topic)
                            .subscribe(this::onNextRawTx, rawTxProcessor::onError, rawTxProcessor::onComplete);
                    break;
                case sequence:
                    getTopicPublisher(topic)
                            .subscribe(this::onNextSequence, this::onError, this::onComplete);
                    break;
                default:
                    throw new RuntimeException("Unknown topic");
            }
        }
    }

    @Override
    public Publisher<byte[]> transactionBinaryPublisher() {
        return rawTxProcessor;
    }

    @Override
    public Publisher<byte[]> transactionHashBinaryPublisher() {
        return hashTxProcessor;
    }

    @Override
    public Publisher<byte[]> blockBinaryPublisher() {
        return rawBlockProcessor;
    }

    @Override
    public Publisher<byte[]> blockHashBinaryPublisher() {
        return hashBlockProcessor;
    }


    @Override
    public void close() {
        zmqContext.close();
    }

    // Get a Publisher for a topic (Flowable is used internally)
    private Flowable<ZMsg> getTopicPublisher(BitcoinZmqMessage.Topic topic) {
        return Flowable.fromPublisher(zmqContext.topicPublisher(topic.toString()));
    }
    
    private Optional<ParsedMessage> parseMessage(ZMsg message) {
        log.debug("New message received.");

        Optional<ParsedMessage> parsedMessage;
        if (message.size() >= 2) {
            BitcoinZmqMessage.Topic topic = BitcoinZmqMessage.Topic.valueOf(new String(message.remove().getData()));
            log.debug("Received {} message.", topic);
            byte[] dataBytes = message.remove().getData();
            if (topic != BitcoinZmqMessage.Topic.sequence)
            {
                long seqNumber = fromByteArray(message.remove().getData());
                log.trace("sequence number {}:{}", topic, seqNumber);
                parsedMessage = Optional.of(new ParsedMessage(topic, dataBytes, seqNumber));
            } else {
                // TODO: Additional sequence parsing?
                parsedMessage = Optional.of(new ParsedMessage(topic, dataBytes, -1));
            }
        } else {
            log.warn("Ignoring message with less than 3 frames");
            parsedMessage = Optional.empty();
        }
        return parsedMessage;
    }

    private void onError(Throwable error) {
        log.error("Seq Error: ", error);
    }

    private void onComplete() {
        log.error("Seq Complete");
    }

    private static class ParsedMessage {
        final BitcoinZmqMessage.Topic topic;
        final byte[] dataBytes;
        final long seqNumber;

        private ParsedMessage(BitcoinZmqMessage.Topic topic, byte[] dataBytes, long seqNumber) {
            this.topic = topic;
            this.dataBytes = dataBytes;
            this.seqNumber = seqNumber;
        }
    }

    private void onNextHashBlock(ZMsg message) {
        Optional<ParsedMessage> opt = parseMessage(message);
        if (opt.isPresent()) {
            ParsedMessage parsed = opt.get();
            processHashBlock(parsed.dataBytes, parsed.seqNumber);
        }
    }

    private void onNextHashTx(ZMsg message) {
        Optional<ParsedMessage> opt = parseMessage(message);
        if (opt.isPresent()) {
            ParsedMessage parsed = opt.get();
            processHashTx(parsed.dataBytes, parsed.seqNumber);
        }
    }

    private void onNextRawBlock(ZMsg message) {
        Optional<ParsedMessage> opt = parseMessage(message);
        if (opt.isPresent()) {
            ParsedMessage parsed = opt.get();
            processRawBlock(parsed.dataBytes, parsed.seqNumber);
        }
    }

    private void onNextRawTx(ZMsg message) {
        Optional<ParsedMessage> opt = parseMessage(message);
        if (opt.isPresent()) {
            ParsedMessage parsed = opt.get();
            processRawTx(parsed.dataBytes, parsed.seqNumber);
        }
    }

    private void onNextSequence(ZMsg message) {
        Optional<ParsedMessage> opt = parseMessage(message);
        if (opt.isPresent()) {
            processSequence(message);
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
