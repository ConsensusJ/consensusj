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
package org.consensusj.daemon.micronaut;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.bitcoinj.base.*;
import org.bitcoinj.crypto.ECKey;
import org.consensusj.bitcoin.json.rpc.BitcoinJsonRpc;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class ApplicationTest {
    @Inject
    EmbeddedServer server;

    Network network;

    BitcoinExtendedClient client;

    @BeforeEach
    void testSetup() {
        client = new BitcoinExtendedClient(server.getURI(), "", "");
        client.connectToServer(Duration.ofSeconds(90)).join();
        network = client.getNetwork();
    }

    @Test
    void testItWorks() {
        assertTrue(server.isRunning());
        assertEquals("http", server.getURI().getScheme());
        assertTrue(server.getURI().getHost().equals("localhost") ||
                    server.getURI().getHost().startsWith("runner")
        );
        assertTrue(server.getEnvironment().getActiveNames().contains("test"));
        assertEquals(BitcoinNetwork.REGTEST, network);
    }

    @Test
    void getNetwork() {
        assertEquals(network, client.getNetwork());
    }

    @Test
    void getBlockCountRequest() throws IOException {
        assertTrue(client.getBlockCount().compareTo(0) >= 0);
    }

    @Test
    void getBlockHashRequest() throws IOException {
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    var txId = client.getBlockHash(0);
                });
        assertEquals("Server exception: Unimplemented RPC method", exception.getMessage());
    }

    @Test
    void helpRequest() throws IOException {
        assertNotNull(client.help());
    }

    @Test
    void getNewAddressRequest() throws IOException {
        assertNotNull(client.getNewAddress());
    }

    @Test
    void getBalanceRequest() throws IOException {
        var balance = client.getBalance();
        assertNotNull(balance);
        assertTrue(balance.compareTo(Coin.ZERO) >= 0);
    }

    @Test
    void listUnspentRequest() throws IOException {
        var unspentList = client.listUnspent(1, BitcoinJsonRpc.DEFAULT_MAX_CONF, List.of());
        assertNotNull(unspentList);
        // Size cannot be negative, is this needed?
        assertTrue(unspentList.size() >= 0);
    }

    @Test
    void sendToAddressRequest() {
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    var txId = client.sendToAddress(randomAddress(), Coin.ofBtc(BigDecimal.valueOf(100)));
                });
        assertTrue(exception.getMessage().startsWith("Server exception: Insufficient money"));
    }

    @Test
    void signRawTransactionWithWallet() {
        JsonRpcStatusException exception =
                assertThrows(JsonRpcStatusException.class, () -> {
                    var txId = client.signRawTransactionWithWallet("0BAD");
                });
        assertTrue(exception.getMessage().startsWith("Server exception: Invalid raw (hex) transaction"));
    }

    private Address randomAddress() {
        return new ECKey().toAddress(ScriptType.P2WPKH, network);
    }
}
