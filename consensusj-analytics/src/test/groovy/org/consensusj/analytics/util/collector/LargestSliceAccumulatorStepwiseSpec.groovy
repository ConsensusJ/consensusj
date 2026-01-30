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


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.function.BinaryOperator
/**
 * Simple, stepwise tests of LargestBalanceAccumulator
 */
@Stepwise
class LargestSliceAccumulatorStepwiseSpec extends Specification {
    static final pair1 = new BalancePair<String, Integer>("pair1", 100)
    static final pair2 = new BalancePair<String, Integer>("pair2", 200)
    static final pair3 = new BalancePair<String, Integer>("pair3", 50)

    @Shared
    LargestSliceAccumulator accum

    def setupSpec() {
        accum = new LargestSliceAccumulator(2, BalancePair::getNumber,0, (BinaryOperator<Integer>) Integer::sum )
    }

    def "constructor check" () {
        expect:
        accum.n == 2
        accum.sortedSliceList.size() == 0
        accum.additionOperator != null
        accum.totalOther == 0
    }

    def "Add one pair" ()  {
        when:
        accum.accumulate(pair1)

        then:
        accum.sortedSliceList.size() == 1
        accum.sortedSliceList.first() == pair1
        accum.totalOther == 0
    }

    def "Add a second (biggest) pair" ()  {
        when:
        accum.accumulate(pair2)

        then:
        accum.sortedSliceList.size() == 2
        accum.sortedSliceList.first() == pair1
        accum.sortedSliceList.last() == pair2
        accum.totalOther == 0
    }

    def "Add a third (smallest) pair" ()  {
        when:
        accum.accumulate(pair3)

        then:
        accum.sortedSliceList.size() == 2
        accum.sortedSliceList.first() == pair1
        accum.sortedSliceList.last() == pair2
        accum.totalOther == 50
    }
}