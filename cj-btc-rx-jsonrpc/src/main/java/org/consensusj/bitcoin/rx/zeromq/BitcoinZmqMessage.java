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

/**
 * Use record-style naming, rather than JavaBean naming, just for fun. (Maybe this will migrate to a record
 * some day.)
 *
 * @param <T>
 */
public class BitcoinZmqMessage<T> {
    private final Topic topic;
    private final T value;
    private final long sequenceNumber;

    public BitcoinZmqMessage(Topic topic, T value, long sequenceNumber) {
        this.topic = topic;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
    }

    public Topic topic() {
        return topic;
    }

    public T value() {
        return value;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Bitcoin ZeroMQ topic names. The {@code toString()} values of these enums are
     * the values used in the wire-protocol (i.e. they are passed to {@link org.zeromq.ZMQ.Socket#subscribe}.
     */
    public enum Topic {
        hashblock,
        hashtx,
        rawblock,
        rawtx,
        sequence  // Not implemented in Bitcoin Core as of 0.20.1, but coming in a future release
    }
}
