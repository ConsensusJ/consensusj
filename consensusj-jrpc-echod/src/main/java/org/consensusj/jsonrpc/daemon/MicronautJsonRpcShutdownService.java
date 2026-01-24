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
package org.consensusj.jsonrpc.daemon;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.consensusj.jsonrpc.JsonRpcShutdownService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: Use or implement GracefulShutdownCapable?
/**
 * Micronaut implementation of {@link JsonRpcShutdownService}. Allows the
 * {@code stop} JSON-RPC method to gracefully shut down the server.
 */
@Singleton
public class MicronautJsonRpcShutdownService implements JsonRpcShutdownService {
    static final String DEFAULT_APPNAME = "server";
    private static final Logger log = LoggerFactory.getLogger(MicronautJsonRpcShutdownService.class);
    private final ApplicationContext applicationContext;
    @Nullable
    private EmbeddedServer embeddedServer;

    MicronautJsonRpcShutdownService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Saving reference to embeddedServer");
        embeddedServer = event.getSource();
    }

    @Override
    public String stopServer() {
        String appName = (embeddedServer != null)
                ? embeddedServer.getApplicationConfiguration().getName().orElse(DEFAULT_APPNAME)
                : DEFAULT_APPNAME;
        applicationContext.stop();  // Shut down the application context which shuts down the embedded server
        return appName + " stopping";
    }
}
