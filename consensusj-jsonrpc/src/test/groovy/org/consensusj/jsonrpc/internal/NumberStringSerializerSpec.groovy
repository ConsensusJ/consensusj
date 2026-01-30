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
