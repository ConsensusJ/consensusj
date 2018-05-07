package com.msgilligan.bitcoinj.json.conversion

import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import spock.lang.Unroll

/**
 * Make sure NumberStringSerializer works for edge-case values
 */
class NumberStringSerializerSpec extends BaseObjectMapperSpec {

    @Unroll
    def "fragment #value serializes as #expectedResult"() {
        when:
        def result = mapper.writeValueAsString(value)

        then:
        result == expectedResult

        where:
        expectedResult              | value
        '-9223372036854775808'      | Long.MIN_VALUE.toString()
        '-1'                        | '-1'
        '0'                         | '0'
        '1'                         | '1'
        '9223372036854775807'       | Long.MAX_VALUE.toString()

        '"-9223372036854775809"'    | ((Long.MIN_VALUE as BigInteger) - 1).toString()
        '"9223372036854775808"'     | ((Long.MAX_VALUE as BigInteger) + 1).toString()
        
        '"a"'                       | 'a'
    }

    def configureModule(module) {
        module.addSerializer(String.class, new NumberStringSerializer())
    }
}
