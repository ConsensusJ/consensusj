/**
 * Reactive interface to <a href="https://zeromq.org">ZeroMQ</a> using <a href="https://github.com/zeromq/jeromq">JeroMQ</a> library.
 * Reactive streams are available via the {@link org.reactivestreams.Publisher} interface, but this will change to
 * {@link java.util.concurrent.Flow.Publisher} before the 1.0 release.
 * It is currently implemented internally with <a href="https://github.com/ReactiveX/RxJava">RxJava 3</a>.
 * Use {@link org.consensusj.rx.zeromq.RxZmqContext} to connect to a server and subscribe to a {@code List} of topics.
 */
package org.consensusj.rx.zeromq;
