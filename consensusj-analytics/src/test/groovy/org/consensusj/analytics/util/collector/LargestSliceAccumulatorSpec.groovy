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
package org.consensusj.analytics.util.collector


import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.BinaryOperator
import java.util.stream.Collectors

/**
 *
 */
class LargestSliceAccumulatorSpec extends Specification {

    def "constructor check" () {
        given:
        def accum = new LargestSliceAccumulator(1, BalancePair::getNumber, 0, (BinaryOperator<Integer>) Integer::sum )

        expect:
        accum.n == 1
        accum.sortedSliceList.size() == 0
        accum.additionOperator != null
        accum.otherTotal == 0
    }

    def "constructor check exception thrown for n < 1" () {
        when:
        def accum = new LargestSliceAccumulator(0, BalancePair::getNumber, 0, (BinaryOperator<Integer>) Integer::sum )

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Accumulator works for Integer singleton pair #address, #value" (String address, Integer value)  {
        given:
        def pair = new BalancePair<String, Integer>(address, value)
        def accum = new LargestSliceAccumulator(1, BalancePair::getNumber, 0, (BinaryOperator<Integer>) Integer::sum )

        when:
        accum.accumulate(pair)

        then:
        accum.sortedSliceList.size() == 1
        accum.sortedSliceList.first() == pair
        
        where:
        address | value
        "1"     | Integer.MIN_VALUE
        "2"     | 100
        "3"     | Integer.MAX_VALUE
    }

    @Unroll
    def "Accumulator (n=1) works for Integer pair list #name" (String name, List<List> list, List expectedValue)  {
        given:
        List<BalancePair<String, Integer>> pairs = list.stream()
                    .map(LargestSliceAccumulatorSpec::listToPair)
                    .collect(Collectors.toList())
        BalancePair<String, Integer> expectedPair = listToPair(expectedValue)
        def accum = new LargestSliceAccumulator(1, BalancePair::getNumber, 0, (BinaryOperator<Integer>) Integer::sum )

        when:
        pairs.forEach(p -> accum.accumulate(p))

        then:
        accum.sortedSliceList.size() == 1
        accum.sortedSliceList.first() == expectedPair
        
        where:
        name            | list                      | expectedValue
        "simple"        | [["a", 1]]                | ["a", 1]
        "two"           | [["a", 1],["b", 2]]       | ["b", 2]
    }

    @Unroll
    def "Accumulator (n=1) works for Consensus balances #name" (String name, List<List> list, List expectedValue)  {
        given:
        List<BalancePair<String, Integer>> pairs = list.stream()
                .map(LargestSliceAccumulatorSpec::listToPair)
                .collect(Collectors.toList())
        BalancePair<String, Integer> expectedPair = listToPair(expectedValue)
        def accum = new LargestSliceAccumulator(1, BalancePair::getNumber, 0, (BinaryOperator<Integer>) Integer::sum )

        when:
        pairs.forEach(p -> accum.accumulate(p))

        then:
        accum.sortedSliceList.size() == 1
        accum.sortedSliceList.first() == expectedPair

        where:
        name            | list                      | expectedValue
        "simple"        | [["a", 1]]                | ["a", 1]
        "two"           | [["a", 1],["b", 2]]       | ["b", 2]
    }

    private static BalancePair<String, Integer> listToPair(List<Object> tuple) {
        return new BalancePair<String, Integer>((String) tuple.get(0), (Integer) tuple.get(1))
    }
}
