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
package org.consensusj.jsonrpc.introspection;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GraalVM-compatible implementation of {@link JsonRpcServiceWrapper} that takes a map of {@link Method}s in the constructor
 * so that introspection can be done at static initialization time (which means during native image generation).
 * Since {@link #getServiceObject()} returns {@code this} you typically directly subclass {@code AbstractJsonRpcService}.
 * For example see {@link org.consensusj.jsonrpc.introspection.sample.MathService}.
 */
public abstract class AbstractJsonRpcService implements JsonRpcServiceWrapper {
    protected final Map<String, Method> methods;

    public AbstractJsonRpcService(Map<String, Method> methods) {
        this.methods = methods;
    }

    @Override
    public Object getServiceObject() {
        return this;
    }

    @Override
    @Nullable
    public Method getMethod(String methodName) {
        return methods.get(methodName);
    }

    protected <RSLT> CompletableFuture<RSLT> result(RSLT result) {
        return CompletableFuture.completedFuture(result);
    }

    protected <RSLT> CompletableFuture<RSLT> exception(Throwable exception) {
        return CompletableFuture.failedFuture(exception);
    }
}
