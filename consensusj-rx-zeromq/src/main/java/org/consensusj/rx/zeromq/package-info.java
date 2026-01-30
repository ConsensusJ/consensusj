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
/**
 * Reactive interface to <a href="https://zeromq.org">ZeroMQ</a> using <a href="https://github.com/zeromq/jeromq">JeroMQ</a> library.
 * Reactive streams are available via the {@link org.reactivestreams.Publisher} interface, but this will change to
 * {@link java.util.concurrent.Flow.Publisher} before the 1.0 release.
 * It is currently implemented internally with <a href="https://github.com/ReactiveX/RxJava">RxJava 3</a>.
 * Use {@link org.consensusj.rx.zeromq.RxZmqContext} to connect to a server and subscribe to a {@code List} of topics.
 */
package org.consensusj.rx.zeromq;
