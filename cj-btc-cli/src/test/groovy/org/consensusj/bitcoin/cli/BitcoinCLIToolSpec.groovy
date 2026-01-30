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
package org.consensusj.bitcoin.cli

import spock.lang.Specification

import java.util.spi.ToolProvider

/**
 *
 */
class BitcoinCLIToolSpec extends Specification {

    def "Can instantiate via ToolProvider"() {
        when:
        def tool = ToolProvider.findFirst("cj-bitcoin-cli").get()

        then:
        tool instanceof BitcoinCLITool
    }

    def "getServerURI works"() {
        given:
        def tool = createInstance()

        when:
        URI expectedURI = "http://localhost:18443".toURI()
        BitcoinCLITool.BitcoinCLICall call = (BitcoinCLITool.BitcoinCLICall) tool.createCall(System.out, System.err, "-regtest", "getblockcount")
        def client = call.rpcClient()
        def serverURI = client.getServerURI()
        def config = call.getRPCConfig()

        then:
        serverURI == expectedURI
        config != null
    }

    private BitcoinCLITool createInstance() {
        return new BitcoinCLITool()
    }

}
