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
package org.consensusj.jsonrpc.services;

import org.consensusj.jsonrpc.JsonRpcShutdownService;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import org.consensusj.jsonrpc.help.JsonRpcHelp;
import java.util.concurrent.CompletableFuture;

/**
 * Simple Echo JSON-RPC Service
 */
public class EchoJsonRpcService extends AbstractJsonRpcService implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(EchoJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final String helpString =
            "echo message\n" +
            "help\n" +
            "stop\n";
    private static final Map<String, JsonRpcHelp> helpMap = Map.of(
            "echo", new JsonRpcHelp("Echo a message back to the caller.",
                    "Usage:\n"
                            + "  echo <message>\n"
                            + "\n"
                            + "Description:\n"
                            + "  Returns the provided message exactly as it was sent.\n"
                            + "\n"
                            + "Parameters:\n"
                            + "  message (string, required)\n"
                            + "    The text to be echoed back by the server.\n"
                            + "\n"
                            + "Example:\n"
                            + "  echo \"hello world\"\n"),
            "help", new JsonRpcHelp("Display help information for a JSON-RPC method.",
                    "Usage:\n"
                            + "  help <method>\n"
                            + "\n"
                            + "Description:\n"
                            + "  Displays detailed help text for the specified method.\n"
                            + "  If the method name is not recognized, a list of available\n"
                            + "  methods and their summaries is returned instead.\n"
                            + "\n"
                            + "Parameters:\n"
                            + "  method (string, required)\n"
                            + "    The name of the method to display help for.\n"
                            + "\n"
                            + "Example:\n"
                            + "  help echo\n"),
            "stop", new JsonRpcHelp("Initiate a graceful shutdown of the server.",
                    "Usage:\n"
                            + "  stop\n"
                            + "\n"
                            + "Description:\n"
                            + "  Initiates the shutdown process of the JSON-RPC server.\n"
                            + "  The server will respond to this request before the\n"
                            + "  shutdown completes, allowing the client to receive\n"
                            + "  confirmation of the action.\n"
                            + "\n"
                            + "Parameters:\n"
                            + "  None.\n"
                            + "\n"
                            + "Warning:\n"
                            + "  Executing this command will make the server unavailable\n"
                            + "  to other clients.\n")
    );


    private final JsonRpcShutdownService shutdownService;

    public EchoJsonRpcService(JsonRpcShutdownService shutdownService) {
        super(methods);
        this.shutdownService = shutdownService;
    }

    @Override
    public void close() {
        log.info("Closing");
    }

    /**
     * Get detailed help information for a given command.
     * @param message: A string containing the message to be echoed
     * @return A string containing the echoed message
     */
    public CompletableFuture<String> echo(String message) {
        log.debug("EchoJsonRpcService: echo {}", message);
        return result(message);
    }

    /**
     * Get detailed help information for a given command.
     * @param method: A string containing the method name
     * @return A string containing help information
     */
    public CompletableFuture<String> help(String method) {
        log.debug("EchoJsonRpcService: help");
        if (helpMap.containsKey(method)) {
            return result(helpMap.get(method).summary());
        } else {
            return result("Method not found.\n" + helpString);
        }
    }

    /**
     * Initiate server shutdown. This is a JSON-RPC method and will initiate but not
     * complete server-shutdown because it must return a response to the client.
     * @return A status string indicating the server is stopping
     */
    public CompletableFuture<String> stop() {
        log.info("stop");
        String message = shutdownService.stopServer();
        return result(message);
    }

}
