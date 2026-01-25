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

import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Singleton that runs early on startup and deletes stale UNIX domain socket.
 */
@Singleton
@Context
public class UnixSocketCleanup implements BeanCreatedEventListener<StartupEvent> {
    private static final Logger log = LoggerFactory.getLogger(UnixSocketCleanup.class);

    private final NettyHttpServerConfiguration serverConfiguration;

    public UnixSocketCleanup(NettyHttpServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        cleanupSockets();
    }

    @Override
    public StartupEvent onCreated(BeanCreatedEvent<StartupEvent> event) {
        // This is called after startup, but we do the cleanup in constructor
        return event.getBean();
    }

    private void cleanupSockets() {
        List<NettyHttpServerConfiguration.NettyListenerConfiguration> listeners = serverConfiguration.getListeners();

        if (listeners != null) {
            for (NettyHttpServerConfiguration.NettyListenerConfiguration listener : listeners) {
                String socketPath = listener.getPath();

                // Check if it's a Unix domain socket (path is not null and not abstract)
                if (socketPath != null && !socketPath.isEmpty() && !socketPath.startsWith("\0")) {
                    Path path = Paths.get(socketPath);
                    log.info("Checking for stale Unix domain socket: {}", path);
                    try {
                        if (Files.deleteIfExists(path)) {
                            log.info("Deleted stale Unix domain socket: {}", path);
                        } else {
                            log.info("Checked for stale Unix domain socket, but not found: {}", socketPath);
                        }
                    } catch (IOException e) {
                        log.error("Failed to delete Unix domain socket: {}", path, e);
                    }
                }
            }
        }
    }
}
