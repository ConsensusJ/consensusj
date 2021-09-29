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
