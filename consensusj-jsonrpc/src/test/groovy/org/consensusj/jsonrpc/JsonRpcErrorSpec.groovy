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
package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 */
class JsonRpcErrorSpec extends Specification {

    def "constructor works correctly" () {
        given:
        def code = 0
        def message = "Message 1"
        def data = new Object()

        when:
        def error = new JsonRpcError(code, message, data)

        then:
        error.code == code
        error.message == message
        error.data == data
    }
    
    def "can deserialize from JSON" () {
        given:
        def jsonString = """
{"code": 0, "message": "Message 1", "data": {}} 
"""
        def mapper = new ObjectMapper()


        when:
        def error = mapper.readValue(jsonString, JsonRpcError.class)

        then:
        error.code == 0
        error.message == "Message 1"
        error.data instanceof Object
    }

}
