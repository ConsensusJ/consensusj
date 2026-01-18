package org.consensusj.daemon.micronaut;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class ServerStopTest {
    @Inject
    EmbeddedServer server;

    BitcoinExtendedClient client;

    @BeforeEach
    void testSetup() {
        client = new BitcoinExtendedClient(server.getURI(), "", "");
    }

    @Disabled
    @Test
    void stop() throws IOException {
        assertEquals("walletd stopping", client.stop());
    }
}
