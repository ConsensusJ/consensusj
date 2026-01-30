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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable implementation of {@link LargestSliceList}.
 */
public final class LargestSliceListImpl<E, N extends Number & Comparable<? super N>> implements LargestSliceList<E,N> {
    private final List<E> list;
    private final N totalOther;

    public LargestSliceListImpl(List<E> list, N totalOther) {
        this.list = Collections.unmodifiableList(new ArrayList<>(list));
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
