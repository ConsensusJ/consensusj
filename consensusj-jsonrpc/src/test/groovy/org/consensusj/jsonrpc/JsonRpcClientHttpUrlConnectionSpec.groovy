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
 * RPCClient test specification
 */
class JsonRpcClientHttpUrlConnectionSpec extends Specification {


    def "constructor works correctly" () {
        when:
        def client = new JsonRpcClientHttpUrlConnection(null, JsonRpcTransport.getDefaultSSLContext(), "http://localhost:8080".toURI(), "user", "pass")

        then:
        client.serverURI == "http://localhost:8080".toURI()
        //client.getJsonRpcVersion() == JsonRpcMessage.Version.V2
    }

    @Unroll
    def "Base64 works for #input"(String input, String expectedResult) {
        expect:
        expectedResult == JsonRpcTransport.base64Encode(input)

        where:
        input               | expectedResult
        "a:b"               | "YTpi"
        "0" * 80            | "MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA="
    }
}
