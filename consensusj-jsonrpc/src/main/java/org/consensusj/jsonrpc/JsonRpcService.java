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
package org.consensusj.jsonrpc;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for an Asynchronous JSON-RPC Server/Service.
 * <p>
 * A service-layer abstraction of JSON-RPC using plain old Java objects (POJOs)
 * Can easily be used in controllers from Java Web frameworks.
 *
 */
public interface JsonRpcService extends AutoCloseable {
    /**
     * Handle a JSON-RPC {@link JsonRpcRequest} and return a {@link JsonRpcResponse} POJO
     * @param req A Request object
     * @param <RSLT> Generic type for the JSON-RPC <b>result</b> in {@link JsonRpcResponse#getResult()}
     * @return a future for a Response object with either a <b>result</b> or an error.
     */
    <RSLT> CompletableFuture<JsonRpcResponse<RSLT>> call(JsonRpcRequest req);
}
