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
package org.consensusj.jsonrpc.groovy

import org.consensusj.jsonrpc.DefaultRpcClient
import org.consensusj.jsonrpc.JsonRpcMessage

import com.fasterxml.jackson.databind.JavaType;

/**
 * Client that uses Groovy <code>methodMissing</code> to allow <i>any</i> JSON-RPC call to be made as <code>client.rpcMethod(args)</code>.
 * Note that calling a non-existent method will result in an error from the server.
 * <p>
 * The focus of JSON-RPC client development in <b>ConsensusJ</b> has been strongly-typed clients implemented in pure Java.
 * We believe that a strongly-typed RPC client is the best choice for most applications and for most integration tests. However,
 * there are many times where a dynamic client is useful and there are users who have a strong preference for a dynamic client.
 * <p>
 * This client and the {@link DynamicRpcMethodFallback} trait are provided for those looking for something simple,
 * flexible, dynamic, and Groovy.
 */
class DynamicRpcClient extends DefaultRpcClient implements DynamicRpcMethodFallback<JavaType> {

    DynamicRpcClient(URI server, String rpcuser, String rpcpassword) {
        this(JsonRpcMessage.Version.V2, server, rpcuser, rpcpassword)
    }

    DynamicRpcClient(JsonRpcMessage.Version jsonRpcVersion, URI server, String rpcuser, String rpcpassword) {
        super(jsonRpcVersion, server, rpcuser, rpcpassword)
    }
}
