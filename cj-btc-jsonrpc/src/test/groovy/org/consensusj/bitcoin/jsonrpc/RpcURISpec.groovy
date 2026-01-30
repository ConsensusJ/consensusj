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
package org.consensusj.bitcoin.jsonrpc

import org.consensusj.bitcoin.jsonrpc.RpcURI
import spock.lang.Specification

/**
 * Check URI constants for correctness
 */
class RpcURISpec extends Specification {
    def "test URI creation methods" () {
        expect:
        RpcURI.defaultMainNetURI == "http://127.0.0.1:8332/".toURI()
        RpcURI.defaultTestNetURI == "http://127.0.0.1:18332/".toURI()
        RpcURI.defaultRegTestURI == "http://127.0.0.1:18443/".toURI()
    }
}
