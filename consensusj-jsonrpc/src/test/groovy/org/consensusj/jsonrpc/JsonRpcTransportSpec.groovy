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

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Basic test of our copied Base64 class.
 */
class JsonRpcTransportSpec extends Specification {

    @Unroll
    def "Base64 Basic Auth Test #myInt" (myInt, expectedResult) {
        given:
        def auth = "myuser" + ":" + "mypass" + myInt;

        when:
        def basicAuth = "Basic " + JsonRpcTransport.base64Encode(auth);

        then:
        basicAuth == expectedResult

        where:
        myInt | expectedResult
        1     | "Basic bXl1c2VyOm15cGFzczE="
        2     | "Basic bXl1c2VyOm15cGFzczI="
        3     | "Basic bXl1c2VyOm15cGFzczM="
        4     | "Basic bXl1c2VyOm15cGFzczQ="
        5     | "Basic bXl1c2VyOm15cGFzczU="
    }
}
