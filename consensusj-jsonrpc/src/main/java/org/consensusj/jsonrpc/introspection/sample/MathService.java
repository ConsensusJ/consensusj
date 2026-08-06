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
package org.consensusj.jsonrpc.introspection.sample;

import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcErrorException;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.consensusj.jsonrpc.help.JsonRpcHelp;
import org.consensusj.jsonrpc.help.JsonRpcHelpText;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Simple service and command-line tool that contains a "math" JsonRpcService
 * Allows testing method dispatch independent of any server code or framework
 * (including compiling with Graal `native-image` and running as a native tool with SubstrateVM)
 * This will be moved or removed in a future release.
 */
public class MathService extends AbstractJsonRpcService {
    private static final Logger log = LoggerFactory.getLogger(MathService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final Map<String, JsonRpcHelp> help = JsonRpcHelp.mapOf(MethodHandles.lookup().lookupClass());
    private final String allHelp;

    /**
     * Constructor that calls {@link AbstractJsonRpcService#AbstractJsonRpcService(Map)} with a private, statically-initialized
     * {@link Map} of methods generated with {@link JsonRpcServiceWrapper#reflect(Class)}  }.
     */
    public MathService() {
        super(methods);
        allHelp = JsonRpcHelp.allMethodsHelp(help);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        JsonRpcService service = new MathService();
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(1, 2));
        JsonRpcResponse<Object> response =  service.call(req).get();

        Integer sum = (Integer) response.getResult();
        if (sum != null) {
            System.out.println("Sum is: " + sum);
        } else {
            String message = response.getError() != null
                    ? response.getError().getMessage()
                    : "error was null";
            System.err.println("Error = " + message);
            System.exit(-1);
        }
    }

    @JsonRpcHelpText(summary = "adds two numbers", details ="This method takes two numeric parameters and returns their sum")
    public CompletableFuture<Integer> add(Integer a, Integer b) {
        log.info("MathService: add {} + {}",a,b);
        return result(a + b);
    }

    @JsonRpcHelpText(summary = "subtracts two numbers", details ="This method takes two numeric parameters and returns their difference")
    public CompletableFuture<Integer> subtract(Integer a, Integer b) {
        log.info("MathService: subtract {} - {}",a,b);
        return result(a - b);
    }

    @JsonRpcHelpText(summary = "help | help <method>", details ="help | help <method>")
    public CompletableFuture<String> help(@Nullable String method) {
        log.info("MathService: help {}", method);
        if (method == null) {
            // Summary help for all methods
            return result(allHelp);
        } else {
            // Detail help for one method (if it exists)
            var methodHelp = help.get(method);
            return (methodHelp != null)
                    ? result(methodHelp.detail())
                    : exception(JsonRpcErrorException.of(JsonRpcError.Error.METHOD_NOT_FOUND));
        }
    }

    @Override
    public void close() throws Exception {
    }
}
