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