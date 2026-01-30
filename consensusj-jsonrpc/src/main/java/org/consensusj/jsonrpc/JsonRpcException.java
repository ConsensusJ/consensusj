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

import java.io.IOException;

/**
 * JSON RPC Exception
 * <p>
 * The {@link JsonRpcErrorException} subclass includes a {@link JsonRpcError} object which can be used
 * in server implementations to pass error information to the server's response serialization layer which
 * can include the {@link JsonRpcError} in the {@link JsonRpcResponse} and in client implementations it can
 * be used for a client to find the {@link JsonRpcError} that was returned.
 * <p>
 * The {@link JsonRpcStatusException} subclass contains support for HTTP response code and message.
 * <p>
 * TODO: Rethink the differences between the two subclasses in light of Bitcoin (and possibly other)
 * implementations returning HTTP status codes along with JsonRpcError responses. Bitcoin even incorrectly
 * returns a 500 for invalid parameters. I think I originally assumed that there would be either an
 * HTTP status error or a JSON-RPC error, but in reality responses can probably have neither, either, or both.
 * @see <a href="https://github.com/bitcoin/bitcoin/issues/2960">Bitcoin Core Issue #2960</a>
 */
public class JsonRpcException extends IOException {

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
