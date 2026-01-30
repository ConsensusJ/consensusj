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
package org.consensusj.rx.zeromq;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZMsg;
import zmq.ZError;

import java.util.concurrent.ThreadFactory;

// TODO: Consider a mechanism to configure backpressure strategy without using RxJava 3. Is there a need?
/**
 *  Factory class for creating {@code Publisher<ZMsg>} from a {@link ZMQ.Socket}. The created {@link Publisher}
 *  will receive a multiplexed stream of {@link ZMsg} (i.e. {@code ZMsg} for all topics.) Typically
 *  these multiplexed {@code Publisher<ZMsg>} are created by a {@link RxZmqContext} which also de-multiplexes them.
 *  <p>
 *  TODO: Write some tests!!
 */
public class ZMsgSocketFlowable {
    private static final Logger log = LoggerFactory.getLogger(ZMsgSocketFlowable.class);
    private static final ThreadFactory defaultThreadFactory = runnable -> new Thread(runnable, "ZeroMQ Receiver");

    static Publisher<ZMsg> createFromSocket(ZMQ.Socket socket) {
        return createFromSocket(socket, defaultThreadFactory);
    }

    /**
     * @deprecated Use {@link #createFromSocket(ZMQ.Socket)}
     */
    @Deprecated
    static Publisher<ZMsg> createFromSocket(ZMQ.Socket socket, BackpressureStrategy backpressureStrategy) {
        return createFromSocket(socket, backpressureStrategy, defaultThreadFactory);
    }

    /**
     * The created {@link Flowable} will create a new receiving loop thread per subscriber, which may
     * not be what you want. To support multiple subscribers use {@link RxZmqContext}, or something
     * similar, or directly use a {@link io.reactivex.rxjava3.processors.FlowableProcessor}
     * <p>
     *     Note: {@code BackpressureStrategy.LATEST} is hard-coded internally
     * 
     * @param socket A connected socket, ready to receive messages with {@link ZMsg#recvMsg(ZMQ.Socket)}
     * @param threadFactory used to create the thread that runs the receive loop
     * @return A "cold" Flowable
     */
    static Publisher<ZMsg> createFromSocket(ZMQ.Socket socket, ThreadFactory threadFactory) {
        return Flowable.create(emitter -> {
            Thread thread = threadFactory.newThread(new ZmqReceiveLoop(socket, emitter));
            thread.start();
            emitter.setCancellable(() -> {
                thread.interrupt();
                thread.join();
            });
        }, BackpressureStrategy.LATEST);
    }

    /**
     * @deprecated Use {@link #createFromSocket(ZMQ.Socket, ThreadFactory)}
     */
    @Deprecated
    static Publisher<ZMsg> createFromSocket(ZMQ.Socket socket, BackpressureStrategy backpressureStrategy, ThreadFactory threadFactory) {
        return Flowable.create(emitter -> {
            Thread thread = threadFactory.newThread(new ZmqReceiveLoop(socket, emitter));
            thread.start();
            emitter.setCancellable(() -> {
                thread.interrupt();
                thread.join();
            });
        }, backpressureStrategy);
    }


    /**
     * A runnable {@link ZMQ.Socket} receive loop that can be cancelled with {@link Thread#interrupt}
     * and sends messages and errors to the supplied {@link FlowableEmitter}.
     */
    private static class ZmqReceiveLoop implements Runnable {
        private final ZMQ.Socket socket;
        private final FlowableEmitter<ZMsg> emitter;

        /**
         * @param socket A connected socket, ready to receive messages with {@link ZMsg#recvMsg(ZMQ.Socket)}
         * @param emitter should be supplied by {link @Flowable#create}
         */
        private ZmqReceiveLoop(ZMQ.Socket socket, FlowableEmitter<ZMsg> emitter) {
            this.socket = socket;
            this.emitter = emitter;
        }

        @Override
        public void run() {
            log.info("Starting receiving thread.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ZMsg message = ZMsg.recvMsg(socket);
                    if (message != null) {
                        emitter.onNext(message);
                    }
                } catch (Exception e) {
                    if (e instanceof ZMQException && ((ZMQException) e).getErrorCode() == ZError.ETERM) {
                        log.info("Got ZError.ETERM");
                        // Is this the error we get when upstream closes the channel?
                        emitter.onComplete();
                    } else if (e instanceof ZMQException && ((ZMQException) e).getErrorCode() == ZError.EINTR) {
                        log.info("Ignoring ZError.EINTR");
                        // Should we send an onComplete when we are cancelled?
                        emitter.onComplete();
                    } else {
                        log.error("Exception: ", e);
                        emitter.onError(e);
                    }
                    break;
                }
            }
            log.info("Closing ZMQ.Socket.");
            socket.setLinger(0);
            socket.close();
        }
    }
}
