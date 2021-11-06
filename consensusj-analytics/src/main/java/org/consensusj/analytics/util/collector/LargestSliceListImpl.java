package org.consensusj.analytics.util.collector;

import java.util.Collections;
import java.util.List;

/**
 * An immutable implementation of {@link LargestSliceList}.
 */
public final class LargestSliceListImpl<E, N extends Number & Comparable<? super N>> implements LargestSliceList<E,N> {
    private final List<E> list;
    private final N totalOther;

    public LargestSliceListImpl(List<E> list, N totalOther) {
        this.list = Collections.unmodifiableList(list);
        this.totalOther = totalOther;
    }
    
    @Override
    public List<E> getSliceList() {
        return list;
    }


    @Override
    public N getTotalOther() {
        return totalOther;
    }
}
