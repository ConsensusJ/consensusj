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
package org.consensusj.jsonrpc.introspection.sample

import org.consensusj.jsonrpc.JsonRpcRequest
import org.consensusj.jsonrpc.JsonRpcResponse
import spock.lang.Ignore
import spock.lang.Specification

/**
 *  Quick test of MathService
 */
class MathServiceTest extends Specification {
    def "Main"() {
        when:
        MathService.main();

        then:
        noExceptionThrown()
    }

    def "add"() {
        given:
        def mathService = new MathService();

        when:
        def sum = mathService.add(2, 1).get()

        then:
        sum == 3
    }

    def "JSON-RPC add"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 3
    }

    def "JSON-RPC subtract"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("subtract", Arrays.asList(2, 1));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 1
    }

    @Ignore
    def "JSON-RPC add of strings"() {
        given:
        def mathService = new MathService();

        when:
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList("2", "1"));
        JsonRpcResponse<Object> response = mathService.call(req).get();
        def sum = response.getResult();

        then:
        sum == 3
    }

}
