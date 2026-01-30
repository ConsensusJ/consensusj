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
 *  Exception wrapper for JsonRpcError
 *  Useful in server implementations, throwing an error that will be transmitted
 *  to the client.
 *  TODO: See TODO in parent class {@link JsonRpcException}
 * @see org.consensusj.jsonrpc.JsonRpcException
 */
public class JsonRpcErrorException extends JsonRpcException {
    private final JsonRpcError error;

    public JsonRpcErrorException(JsonRpcError error) {
        super(error.getMessage());
        this.error = error;
    }

    public JsonRpcErrorException(JsonRpcError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }

    public JsonRpcErrorException(JsonRpcError.Error code, Throwable cause) {
        super(code.getMessage() + ": " + cause.getMessage(), cause);
        this.error = JsonRpcError.of(code, cause);
    }

    /**
     * Get the JSON RPC Error POJO
     * @return An error object
     */
    public JsonRpcError getError() {
        return error;
    }

    public static JsonRpcErrorException of(JsonRpcError.Error code) {
        return new JsonRpcErrorException(JsonRpcError.of(code));
    }
}
