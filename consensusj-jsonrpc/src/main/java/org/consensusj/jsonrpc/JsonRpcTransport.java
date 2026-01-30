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

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Defines the interface for a network-layer implementation of a JSON-RPC client.
 * @param <T> Type that can be used (in addition to {@link Class}) to declare expected result types for JSON-RPC method calls.
 */
public interface JsonRpcTransport<T extends Type> extends AsyncSupport {
    /**
     * Get the URI of the remote server
     * @return URI of remote server
     */
    URI getServerURI();

    /**
     * Send a {@link JsonRpcRequest} for a {@link JsonRpcResponse}
     * <p>Synchronous subclasses should override this method to prevent {@link CompletableFuture#supplyAsync(Supplier)} from
     * being called twice when {@link AsyncSupport} is being used. Eventually we'll migrate more of the codebase to native
     * async, and then we won't have to worry about calling {@code supplyAsync} twice.
     * @param <R> Type of result object
     * @param request The request to send
     * @param responseType The response type expected (used by Jackson for conversion)
     * @return A JSON RPC Response with `result` of type `R`
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    default <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, T responseType) throws IOException, JsonRpcStatusException {
        return syncGet(sendRequestForResponseAsync(request, responseType));
    }

    /**
     * Send a {@link JsonRpcRequest} for a {@link JsonRpcResponse} asynchronously.
     * @param <R> Type of result object
     * @param request The request to send
     * @param responseType The response type expected (used by Jackson for conversion)
     * @return A future JSON RPC Response with `result` of type `R`
     */
    <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, T responseType);

    /**
     * Synchronously complete a JSON-RPC request by calling {@link CompletableFuture#get()}, unwrapping nested
     * {@link JsonRpcException} or {@link IOException} from {@link ExecutionException}.
     * @param future The {@code CompletableFuture} (result of JSON-RPC request) to unwrap
     * @return A JSON-RPC result
     * @param <R> The expected result type
     * @throws IOException If {@link CompletableFuture#get} threw  {@code ExecutionException} caused by {@code IOException}
     * @throws JsonRpcException If {@link CompletableFuture#get} threw  {@code ExecutionException} caused by {@code JsonRpcException}
     * @throws RuntimeException If {@link CompletableFuture#get} threw {@link InterruptedException} or other {@link ExecutionException}.
     */
    default <R> R syncGet(CompletableFuture<R> future) throws IOException, JsonRpcException {
        try {
            return future.get();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof JsonRpcException) {
                throw (JsonRpcException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(ee);
            }
        }
    }

    /**
     * Encode username password as Base64 for basic authentication
     * <p>
     * We're using {@link java.util.Base64}, which requires Android 8.0 or later.
     *
     * @param authString An authorization string of the form `username:password`
     * @return A compliant Base64 encoding of `authString`
     */
    static String base64Encode(String authString) {
        return Base64.getEncoder().encodeToString(authString.getBytes()).trim();
    }

    /**
     *
     * Return the default {@link SSLContext} without declaring a checked exception
     * @return The default {@code SSLContext}
     */
    static SSLContext getDefaultSSLContext() {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
