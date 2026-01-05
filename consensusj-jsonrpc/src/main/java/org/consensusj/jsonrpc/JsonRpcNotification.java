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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonRpcNotification {
    private final String  jsonrpc;
    private final String  method;
    private final List<@Nullable Object> params;


    @JsonCreator
    public JsonRpcNotification(@JsonProperty("jsonrpc")  String jsonrpc,
                               @JsonProperty("method")   String method,
                               @JsonProperty("params")   List<@Nullable Object> params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
    }

    public JsonRpcNotification(String method, List<@Nullable Object> params) {
        this(JsonRpcMessage.Version.V2.jsonrpc(), method, params);
    }

    public JsonRpcNotification(String method) {
        this(method, List.of());
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public List<@Nullable Object> getParams() {
        return params;
    }
}
