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

/**
 * Implementations of this interface initiate shutting down the (server) application
 * that is hosting a {@link JsonRpcService}. It is expected that the server-shutdown process
 * will in turn call the {@link JsonRpcService#close()} method at a later time.
 */
public interface JsonRpcShutdownService {
    /**
     * This method will initiate a server shutdown. Implementations MUST not shut down
     * until after the caller of this method is able to return a response to a client.
     * @return A shutdown message (e.g. {@code "echod stopping"}) to return to the client.
     */
    String stopServer();

    /**
     * Trivial implementation for simple servers, unit tests, etc.
     */
    class NoopShutdownService implements JsonRpcShutdownService {
        @Override
        public String stopServer() {
            return "server stop received (stop behavior is default/undefined)";
        }
    }
}
