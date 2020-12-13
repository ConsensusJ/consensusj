package org.consensusj.rx.zeromq;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * ZMQ Publisher that subscribes {@code SUB} to one or more topics using ZMQ and publishes those
 * topics as RxJava3 {@link Flowable}s.
 * Topics (at least for now) must be passed to the constructor.
 * It would be nice to be able subscribe to topics at the ZMQ level when we first get
 * subscriptions at the Rx level. This will require using a ZPoller or something like that.
 */
public class ZmqTopicPublisher implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(ZmqTopicPublisher.class);
    private final URI tcpAddress;
    private final List<String> topics;
    private final ZContext zContext;
    private final Disposable disposable;

    private final ConcurrentHashMap<String, FlowableProcessor<ZMsg>> processors = new ConcurrentHashMap<>();

    public ZmqTopicPublisher(URI tcpAddress, List<String> topics) {
        this(tcpAddress, topics, (Runnable r) -> new Thread(r, "ZeroMQ Subscriber"));
    }

    public ZmqTopicPublisher(URI tcpAddress, List<String> topics, ThreadFactory threadFactory) {
        this.tcpAddress = tcpAddress;
        this.topics = topics;
        topics.forEach(this::addTopic);
        zContext = new ZContext();
        //  Socket to talk to server
        log.info("Connecting to Zmq server");

        ZMQ.Socket socket = zContext.createSocket(SocketType.SUB);
        socket.connect(tcpAddress.toString());

        topics.forEach(topic -> {
            log.info("Subscribing to topic: {}", topic);
            socket.subscribe(topic);
        });

        disposable = ZmqFlowable.createFromSocket(socket, BackpressureStrategy.LATEST, threadFactory)
                .subscribe(this::onNext, this::onError, this::onComplete);
        log.info("Connected.. Waiting for subscribers.");
    }

    /**
     * Get a Publisher (Flowable) for a given topic
     *
     * @param topic a valid topic as passed to the constructor
     * @return A Publisher (Flowable) for the topic
     * @throws IllegalArgumentException if topic wasn't configured in the constructor
     */
    public Flowable<ZMsg> topicPublisher(String topic) {
        Flowable<ZMsg> flowable = processors.get(topic);
        if (flowable == null) {
            throw new IllegalArgumentException("topic unavailable -- topics must be passed to constructor");
        }
        return flowable;
    }

    @Override
    public void close() {
        disposable.dispose();
        zContext.close();
    }

    private void onNext(ZMsg zMsg) {
        // Deliver message to correct processor for topic
        String topic = zMsg.getFirst().getString(StandardCharsets.UTF_8);
        Subscriber<ZMsg> subscriber = processors.get(topic);
        subscriber.onNext(zMsg);
    }

    private void onError(Throwable throwable) {
        // Propagate upstream onError to all downstream
        processors.values().forEach(p -> p.onError(throwable));
    }

    private void onComplete() {
        // Propagate upstream onComplete to all downstream
        processors.values().forEach(Subscriber::onComplete);
    }

    private void addTopic(String topic) {
        processors.computeIfAbsent(topic, t -> PublishProcessor.create());
    }
}
