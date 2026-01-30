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

import org.consensusj.jsonrpc.JsonRpcClient

import java.util.concurrent.CompletableFuture

/**
 * Groovy trait that adds dynamic JSON-RPC method fallback to any JSON-RPC client.
 * <p>
 * Any subclass of {@link JsonRpcClient} can use {@code implements DynamicRpcMethodFallback} to add
 * a <b>Groovy</b> <a href="https://docs.groovy-lang.org/latest/html/documentation/#_methodmissing">methodMissing</a>
 * implementation that will call the JSON-RPC server with the missing method name and provided args.
 * <p>
 * To define a class with dynamic method fallback, use a declaration that looks like the following:
 * <pre>
 * class MyClient extends BaseClient implements DynamicRpcMethodFallback
 * </pre>
 * <p>
 * Groovy also allows <a href="https://docs.groovy-lang.org/latest/html/documentation/#_implementing_a_trait_at_runtime">implementing a trait at runtime</a>.
 * This means you can dynamically add this {@code trait} to any class that extends {@link JsonRpcClient} using the Groovy
 * {@code as} keyword. For example if {@code staticClient} is an instance of a subclass of {@code JsonRpcClient},
 * the following snippet creates a <q>decorated</q> object ({@code dynamicClient}) that has all the methods
 * of {@code staticClient} <i>and</i> will call the server for any <q>missing</q> methods:
 * <pre>
 *     var dynamicClient = staticClient as DynamicRpcMethodFallback
 * </pre>
 *
 * @see <a href="https://docs.groovy-lang.org/latest/html/documentation/#_methodmissing">Groovy Language Documentation: methodMissing</a>
 * @see <a href="https://docs.groovy-lang.org/latest/html/documentation/#_implementing_a_trait_at_runtime">Groovy Language Documentation: Implementing a trait at runtime</a>
 * @param <T> Type that can be used (in addition to {@link Class}) to declare expected result types for JSON-RPC method calls.
 */
trait DynamicRpcMethodFallback<T> implements JsonRpcClient {
    /**
     * Dynamically forward missing method calls to the server and return a result.
     *
     * @param name The JSON-RPC method name
     * @param args JSON-RPC arguments
     * @return an object containing the JSON-RPC response.result
     * @throws org.consensusj.jsonrpc.JsonRpcStatusException
     * @see <a href="https://docs.groovy-lang.org/latest/html/documentation/#_methodmissing">methodMissing</a>
     */
    def methodMissing(String name, def args) {
        CompletableFuture<Object> future = sendAsync(name, args as List)
        Object result = syncGet(future)
        return result
    }
}
