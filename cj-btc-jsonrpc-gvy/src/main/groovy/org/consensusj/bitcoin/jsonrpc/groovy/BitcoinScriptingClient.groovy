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
package org.consensusj.bitcoin.jsonrpc.groovy

import com.fasterxml.jackson.databind.JavaType
import org.bitcoinj.base.Network
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.jsonrpc.groovy.DynamicRpcMethodFallback

/**
 * Bitcoin RPC client for scripting. Allows dynamic methods to access new RPCs or RPCs not implemented in Java client
 */
class BitcoinScriptingClient extends BitcoinExtendedClient implements DynamicRpcMethodFallback<JavaType> {

    /**
     * No args constructor reads bitcoin.conf
     */
    BitcoinScriptingClient() {
        super()
    }

    BitcoinScriptingClient(Network network, URI server, String rpcuser, String rpcpassword) {
        super(network, server, rpcuser, rpcpassword);
    }
}
