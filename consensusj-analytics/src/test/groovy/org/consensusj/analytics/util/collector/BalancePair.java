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

/**
 * A token address and balance
 * @param <K> key (token address)
 * @param <N> numeric balance value
 */
public class BalancePair<K extends Comparable<K>, N extends Number & Comparable<? super N>> implements Comparable<BalancePair<K,N>> {
    private final K key;
    private final N number;

    public BalancePair(K key, N number) {
        this.key = key;
        this.number = number;
    }

    public K getKey() {
        return key;
    }

    public N getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalancePair that = (BalancePair) o;
        return this.key.equals(that.key) && this.number.equals(that.number);
    }

    @Override
    public String toString() {
        return key.toString() + ":" + number.toString();
    }

    @Override
    public int compareTo(BalancePair<K, N> o) {
        int result = getNumber().compareTo(o.getNumber());
        // If numbers are equal, we must sort by the key itself, we don't
        // want Collections to filter out identical balances as duplicates
        if (result != 0) {
            return result;
        } else {
            return getKey().compareTo(o.getKey());
        }
    };
}
