package org.consensusj.jsonrpc.internal

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Make sure NumberStringSerializer works for edge-case values
 */
class NumberStringSerializerSpec extends Specification {
    @Shared
    def mapper

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
    
    def setup() {
        mapper = new ObjectMapper()
        def testModule = new SimpleModule("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null))
        configureModule(testModule)
        mapper.registerModule(testModule)
    }
}
