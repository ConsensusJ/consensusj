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
 * In the future this may be a superclass for {@link JsonRpcRequest} and {@link JsonRpcResponse}.
 * For now it just contains the {@code Version enum}.
 */
public interface JsonRpcMessage {
    enum Version {
        V1("1.0"),
        V2("2.0");

        private final String jsonrpc;

        /**
         * Constructor with value for the {@code jsonrpc} message field.
         *
         * @param jsonrpc The value of the {@code jsonrpc} field in a JSON-RPC message
         */
        Version(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        /**
         * Get the value for the message field.
         *
         * @return the value for the {@code jsonrpc} message field
         */
        public String jsonrpc() {
            return jsonrpc;
        }

        public String toString() {
            return jsonrpc;
        }
    }
}
