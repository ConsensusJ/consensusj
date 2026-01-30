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
package org.consensusj.analytics.util.collector;


import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector that collects the {@code n} objects containing the "largest slices" of an input stream of
 * type {@code <E>}. Each object must have a numeric "slice size" of type {@code <N>} determined by a {@code sliceSizeExtractor}
 * provided to the constructor. The result is a {@link LargestSliceList} containing the {@code n} largest slices
 * ({@link LargestSliceList#getSliceList()}) and the sum of the slice sizes ({@link LargestSliceList#getTotalOther()}) of
 * the filtered/excluded objects. The idea is the results can easily be converted to a Pie Chart.
 * <p>
 * The objects streamed must be unique, non-null, and a {@code sliceSizeExtractor} must be provided that
 * will return a {@link Comparable} subclass of {@link Number}. The internal {@link LargestSliceAccumulator} is
 * a mutable implementation of {@link LargestSliceList}
 * <p>
 * Note: Be careful to use only a single {@code sliceSizeExtractor} or you can end up with {@link LargestSliceList} objects
 * with incompatible search orders (comparators).
 * 
 * @param <E> The type of objects
 * @param <N> The subclass of {@link Number} to use, must be {@link Comparable}
 */
public class LargestSliceCollector<E, N extends Number & Comparable<? super N>>
        implements Collector<E, LargestSliceAccumulator<E, N>, LargestSliceList<E, N>> {
    private final int n;   // Maximum number to track
    private final N zero;
    private final Function<E, N> sliceSizeExtractor;
    private final BinaryOperator<N> additionOperator;
    
    /**
     * Construct a {@code LargestSliceCollector} to collect {@code n} objects of type {@code <E>}. The remaining
     * constructor parameters provide support for the necessary internal calculations for types {@code <E>} and {@code <N>}.
     *
     * @param n Number of "slices" to collect
     * @param sliceSizeExtractor A {@link Function} to return the slice size of an object
     * @param zero The value of zero for type N
     * @param additionOperator addition operator for type N
     */
    public LargestSliceCollector(int n, Function<E, N> sliceSizeExtractor, N zero, BinaryOperator<N> additionOperator) {
        this.n = n;
        this.sliceSizeExtractor = sliceSizeExtractor;
        this.zero = zero;
        this.additionOperator = additionOperator;
    }

    @Override
    public Supplier<LargestSliceAccumulator<E, N>> supplier() {
        return () -> new LargestSliceAccumulator<>(n, sliceSizeExtractor, zero, additionOperator);
    }

    @Override
    public BiConsumer<LargestSliceAccumulator<E, N>, E> accumulator() {
        return LargestSliceAccumulator::accumulate;
    }

    @Override
    public BinaryOperator<LargestSliceAccumulator<E, N>> combiner() {
        return LargestSliceAccumulator::combine;
    }


    /**
     * Return a finisher function.
     *
     * @return A finisher function
     */
    @Override
    public Function<LargestSliceAccumulator<E, N>, LargestSliceList<E, N>> finisher() {
        return (accum) -> new LargestSliceListImpl<>(accum.getSortedSliceList(), accum.getTotalOther());
    }

    /**
     * Note that our accumulator won't work correctly <b>or</b> efficiently if a single accumulator
     * is used from multiple threads. It works with parallel streams, but not with a CONCURRENT collector.
     * See: https://stackoverflow.com/questions/22350288/parallel-streams-collectors-and-thread-safety
     * 
     * @return The characteristics of this collector.
     */
    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
