package org.consensusj.analytics.util.collector;

import java.util.List;

/**
 * Represents a filtered subset of a collection or stream of objects.
 * @see LargestSliceCollector
 */
public interface LargestSliceList<E, N extends Number & Comparable<? super N>> {
    List<E> getSliceList();
    N getTotalOther();
}
