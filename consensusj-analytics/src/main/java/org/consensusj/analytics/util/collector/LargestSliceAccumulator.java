package org.consensusj.analytics.util.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Accumulates the {@code n} largest slices of a stream of objects of type {@code <E>}. Uses a {@link PriorityQueue}
 * implementation of {@link AbstractQueue} internally.
 * <p>
 * This class is <b>not</b> thread-safe and is <b>not</b> designed for use with a {@link java.util.stream.Collector.Characteristics#CONCURRENT}
 * {@link java.util.stream.Collector}. Switching the {@link AbstractQueue} implementation to {@link java.util.concurrent.PriorityBlockingQueue}
 * will work (with reduced performance) but the implementations of {@link #accumulate(Object)}, {@link #combine(LargestSliceAccumulator)},
 * and {@link #drain()} are not thread-safe. Even if this class were thread-safe, enabling {@code CONCURRENT}
 * in a {@code Collector} using this class would <b>reduce</b> performance because multiple threads would be trying to merge Objects
 * into the same accumulator. The optimal way to use this class is with one accumulator per thread
 * and this is what Collector will do without the {@code CONCURRENT} flag.
 * 
 * @param <E> must be comparable because it is the second sort field
 * @param <N> numeric type for Slice Size
 */
public final class LargestSliceAccumulator<E, N extends Number & Comparable<? super N>>  {
    private static final Logger log = LoggerFactory.getLogger(LargestSliceAccumulator.class);
    private final int n;  // Maximum number of slices to track
    private final Function<E, N> sliceSizeExtractor;
    private final BinaryOperator<N> additionOperator;
    private final AbstractQueue<E> sliceQueue;
    private final Comparator<E> comparator;
    private N otherTotal;
    
    /**
     * Construct
     * @param n maximum number of keys (addresses) to track
     * @param sliceSizeExtractor Function to compute the slice size
     * @param zero The value zero for type N
     * @param additionOperator binary addition operator for type N
     */
    public LargestSliceAccumulator(int n,
                                   Function<E, N> sliceSizeExtractor,
                                   N zero,
                                   BinaryOperator<N> additionOperator) {
        if (n < 1) {
            throw new IllegalArgumentException("parameter must be 1 or greater");
        }
        this.n = n;
        this.sliceSizeExtractor = sliceSizeExtractor;
        this.additionOperator = additionOperator;
        this.otherTotal = zero;
        this.comparator = Comparator.comparing(sliceSizeExtractor);
        log.trace("Creating accum queue");
        sliceQueue = new PriorityQueue<>(n, Comparator.comparing(sliceSizeExtractor));
    }

    /**
     * Add a new slice to the accumulator
     *
     * @param newSlice slice to accumulate
     */
    void accumulate(E newSlice) {
        //log.trace("accumulating slice of size: {}", sliceSizeExtractor.apply(newSlice).doubleValue());
        sliceQueue.add(newSlice);
        drain();    // Remove extra elements.
    }

    /**
     * Combine two accumulators
     *
     * @param other the other accumulator
     * @return the combined accumulator (this)
     */
    LargestSliceAccumulator<E, N> combine(LargestSliceAccumulator<E, N > other) {
        other.sliceQueue.forEach(this::accumulate);
        otherTotal = plus(otherTotal, other.otherTotal);
        return this;
    }

    // It's simpler (and possibly faster) to let the queue do the size comparison
    // and just remove extra elements rather than implement our own checks _before_
    // adding to the queue.
    private void drain() {
        while (sliceQueue.size() > n) {
            E removed = sliceQueue.poll();
            if (removed != null) {
                otherTotal = plus(otherTotal, sliceSizeExtractor.apply(removed));
            }
        }
    }
    
    /**
     * Sort the sliceQueue and return as a {@link List}.
     * Normally, This should only be called by the Collector finisher function.
     * 
     * @return List of slices sorted by their slice size (based on extractor)
     */
    List<E> getSortedSliceList() {
        return sliceQueue.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Get the total "slice size" of processed elements that are not saved in this accumulator.
     *
     * @return total slice size of other elements.
     */
    N getTotalOther() {
        return otherTotal;
    }

    private N plus(N n1, N n2) {
        return additionOperator.apply(n1, n2);
    }
}
