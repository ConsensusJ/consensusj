package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZMQ Subscriber that listens to one or more topics.
 * Topics (at least for now) must be passed to the constructor.
 * It would be nice to subscribe to topics at the ZMQ level when we first get
 * subscriptions at the Rx level. This will require using a ZPoller or some thing like that.
 */
public class ZmqSubscriber implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ZmqSubscriber.class);
    private final URI tcpAddress;
    private final List<String> topics;
    private final Thread thread;

    private final ConcurrentHashMap<String, PublishProcessor<ZMsg>> processors = new ConcurrentHashMap<>();

    public ZmqSubscriber(URI tcpAddress, List<String> topics) {
        this.tcpAddress = tcpAddress;
        this.topics = topics;
        topics.forEach(this::addTopic);
        thread = new Thread(new ReceivingThread(), "ZeroMQ Subscriber");
        thread.start();
    }

    private class ReceivingThread implements Runnable {
        @Override
        public void run() {
            try (ZContext context = new ZContext()) {
                //  Socket to talk to server
                log.info("Connecting to Zmq server");

                ZMQ.Socket socket = context.createSocket(SocketType.SUB);
                socket.connect(tcpAddress.toString());

                topics.forEach(topic -> {
                    log.info("Subscribing to topic: {}", topic);
                    socket.subscribe(topic);
                });

                log.info("Connected.. Waiting for subscribers.");

                while (!Thread.currentThread().isInterrupted()) {
                    ZMsg message = ZMsg.recvMsg(socket);
                    String topic = message.getFirst().getString(StandardCharsets.UTF_8);
                    PublishProcessor<ZMsg> processor = processors.get(topic);
                    processor.onNext(message);
                }
            }
        }
    }

    private void addTopic(String topic) {
        processors.computeIfAbsent(topic, t -> PublishProcessor.create());
    }

    @Override
    public void close() {
        thread.interrupt();
    }

    public Observable<ZMsg> observableTopic(String topic) {
        PublishProcessor<ZMsg> processor = processors.get(topic);
        if (processor == null) {
            throw new IllegalArgumentException("topic unavailable -- must be passed to constructor");
        }
        return processor.toObservable();
    }
}
