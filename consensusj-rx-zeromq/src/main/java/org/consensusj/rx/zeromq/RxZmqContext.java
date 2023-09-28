package org.consensusj.rx.zeromq;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.reactivestreams.Publisher;
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
 * ZMQ Context with a single socket and one {@code Processor<ZMsg>} per topic.
 * It subscribes {@code SUB} to one or more topics using ZMQ and receives a multiplexed
 * stream of {@link ZMsg} from the {@link ZMsgSocketFlowable}. In {@link RxZmqContext#onNext} the {@code ZMsg}
 * are de-multiplexed by the {@code topic} {@code String} and placed in a per-topic
 * {@code Processor<ZMsg>}.
 * <p>
 * Topics (at least for now) must be passed to the constructor.
 * It would be nice to be able to subscribe to topics at the ZMQ level when we first get
 * subscriptions at the Rx level. This will probably require using a ZPoller or something like that.
 */
public class RxZmqContext implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(RxZmqContext.class);
    private final ZContext zContext;
    private final Disposable disposable;

    // Map of topic names to per-topic Processor
    private final ConcurrentHashMap<String, FlowableProcessor<ZMsg>> processors = new ConcurrentHashMap<>();

    public RxZmqContext(URI tcpAddress, List<String> topics) {
        this(tcpAddress, topics, (Runnable r) -> new Thread(r, "ZeroMQ Subscriber"));
    }

    /**
     * @param tcpAddress Address of ZMQ server to connect to
     * @param topics a list of topics to subscribe to
     * @param threadFactory factory to create a thread for the receive loop (see {@link ZMsgSocketFlowable#createFromSocket(ZMQ.Socket, ThreadFactory)})
     */
    public RxZmqContext(URI tcpAddress, List<String> topics, ThreadFactory threadFactory) {
        zContext = new ZContext();
        //  Socket to talk to server
        log.info("Connecting to Zmq server at: {}", tcpAddress);

        ZMQ.Socket socket = zContext.createSocket(SocketType.SUB);
        socket.connect(tcpAddress.toString());

        topics.forEach(topic -> {
            log.info("Subscribing to topic: {}", topic);
            this.addTopic(topic);
            socket.subscribe(topic);
        });

        disposable = Flowable.fromPublisher(ZMsgSocketFlowable.createFromSocket(socket, threadFactory))
                .subscribe(this::onNext, this::onError, this::onComplete);
        log.info("Connected.. Waiting for subscribers.");
    }

    /**
     * Get a Publisher (internally is an RxJava3 Flowable) for a given topic
     *
     * @param topic a valid topic as passed to the constructor
     * @return A Publisher for the topic
     * @throws IllegalArgumentException if topic wasn't configured in the constructor
     */
    public Publisher<ZMsg> topicPublisher(String topic) {
        Publisher<ZMsg> publisher = processors.get(topic);
        if (publisher == null) {
            throw new IllegalArgumentException("topic unavailable -- topics must be passed to constructor");
        }
        return publisher;
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
        processors.computeIfAbsent(topic, keyTopic -> PublishProcessor.create());
    }
}
