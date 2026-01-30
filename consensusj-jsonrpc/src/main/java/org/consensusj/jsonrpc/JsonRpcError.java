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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * JSON-RPC Error Object POJO
 */
public class JsonRpcError {
    private final int code;
    private final String message;
    @Nullable
    private final Object data;


    @JsonCreator
    public JsonRpcError(@JsonProperty("code") int code,
                        @JsonProperty("message") String message,
                        @Nullable @JsonProperty("data") Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private JsonRpcError(Error code) {
        this.code = code.code;
        this.message = code.message;
        this.data = null;
    }

    private JsonRpcError(Error code, Throwable throwable) {
        this.code = code.code;
        this.message = code.message + ": " + throwable.getMessage();
        this.data = null;
    }

    public static JsonRpcError of(Error code) {
        return new JsonRpcError(code);
    }

    public static JsonRpcError of(Error code, Throwable throwable) {
        return new JsonRpcError(code, throwable);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    
    @Nullable
    public Object getData() {
        return data;
    }

    /**
     * Predefined JSON-RPC Error Codes. The error codes from and including -32768 to -32000 are reserved for pre-defined errors.
     * @see <a href="https://www.jsonrpc.org/specification#error_object">5.1 Error object</a> in <b>JSON-RPC 2.0 Specification</b>
     */
    public enum Error {
        PARSE_ERROR(-32700, "Parse error"),
        INVALID_REQUEST(-32600, "Invalid Request"),
        METHOD_NOT_FOUND(-32601, "Method not found"),
        INVALID_PARAMS(-32602, "Invalid params"),
        INTERNAL_ERROR(-32603, "Internal error"),
        SERVER_ERROR(-32000, "Server error"),
        SERVER_EXCEPTION(-32001, "Server exception");

        private final int code;
        private final String message;

        Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
