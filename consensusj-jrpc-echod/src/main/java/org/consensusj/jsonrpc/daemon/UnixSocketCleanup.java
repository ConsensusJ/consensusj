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
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration.NettyListenerConfiguration;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Singleton that runs early on startup and deletes stale UNIX domain sockets.
 */
@Singleton
@Context
public class UnixSocketCleanup implements BeanCreatedEventListener<StartupEvent> {
    private static final Logger log = LoggerFactory.getLogger(UnixSocketCleanup.class);

    public UnixSocketCleanup(NettyHttpServerConfiguration serverConfiguration) {
        // Remove stale socket files as soon as we are constructed, before the Netty server starts
        cleanupSockets(serverConfiguration);
    }

    @Override
    public StartupEvent onCreated(BeanCreatedEvent<StartupEvent> event) {
        // This is called after startup, but we do the cleanup in constructor
        return event.getBean();
    }

    /**
     * Delete all <i>stale</i> UNIX socket files from the previous run (in case there was not a clean shutdown.)
     * Stream the list of {@link NettyListenerConfiguration}s from the {@link NettyHttpServerConfiguration}, find
     * all UNIX socket file paths and delete them.
     * <p>
     * This method should be called as early as possible in the startup process: <b>after</b> {@link NettyHttpServerConfiguration}
     * is available and <b>before</b> the listeners are actually started.
     * @param serverConfiguration The Netty HTTP server configuration
     */
    private static void cleanupSockets(NettyHttpServerConfiguration serverConfiguration) {
        List<NettyListenerConfiguration> listeners = serverConfiguration.getListeners();
        if (listeners != null) {
            listeners.stream()
                .map(NettyListenerConfiguration::getPath)
                // Filter non-UNIX socket listeners/filesystem-paths (paths starting with '\0' are Linux "abstract paths")
                .filter(ps -> ps != null && !ps.isEmpty() && !ps.startsWith("\0") )
                .map(Paths::get)
                .forEach(path -> {
                    try {
                        if (Files.deleteIfExists(path)) {
                            log.info("Deleted stale Unix domain socket: {}", path);
                        }
                    } catch (IOException e) {
                        log.error("Failed to delete Unix domain socket: {}", path, e);
                    }
                });
        }
    }
}
