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

import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Quick test of JsonRpcServiceWrapper
 */
class JsonRpcServiceWrapperTest extends Specification {
    def "reflect works"() {
        given:
        def unwrapped = new TrivialJsonRpcService()

        when:
        def result = JsonRpcServiceWrapper.reflect(unwrapped.class)

        then:
        // TODO: size is unknown (bigger than 1) because of inherited methods from Object
        result.size() >= 1
        result.get("getblockcount") instanceof Method
    }
}
