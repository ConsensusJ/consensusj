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
package org.consensusj.jsonrpc.introspection

import org.consensusj.jsonrpc.JsonRpcRequest
import org.consensusj.jsonrpc.JsonRpcResponse
import spock.lang.Specification

/**
 * Quick smoke test of DelegatingJsonRpcService
 */
class DelegatingJsonRpcServiceTest extends Specification {
    def "callMethod works"() {
        given:
        var unwrapped = new TrivialJsonRpcService()
        var wrapped = new DelegatingJsonRpcService(unwrapped)

        when:
        JsonRpcResponse<Integer> response = wrapped.call(new JsonRpcRequest("getblockcount")).get()

        then:
        response.result == 99
    }
}
