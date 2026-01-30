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
package org.consensusj.bitcoin.integ.java;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.jsonrpc.RpcURI;
import org.consensusj.bitcoin.jsonrpc.test.TestServers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Java RegTest that mines RegTest coins and sends them to a WalletAppKit
 */
@EnabledIfSystemProperty(named = "regTestUseLegacyWallet", matches = "true")
public class WalletAppKitRegTest {
    static final BitcoinNetwork network = BitcoinNetwork.REGTEST;
    static final private TestServers testServers = TestServers.getInstance();
    static final protected String rpcTestUser = testServers.getRpcTestUser();
    static final protected String rpcTestPassword = testServers.getRpcTestPassword();

    private static BitcoinExtendedClient client;
    WalletAppKit kit;

    @BeforeAll
    static void setupRegTestWallet() {
        client = new BitcoinExtendedClient(network, RpcURI.getDefaultRegTestWalletURI(), rpcTestUser, rpcTestPassword);
        client.initRegTestWallet();
    }

    @BeforeEach
    void setupTest(@TempDir File tempDir) throws UnknownHostException {
        kit = WalletAppKit.launch(network, tempDir, "prefix");
    }

    @AfterEach
    void cleanupTest() throws TimeoutException {
        kit.stopAsync();
        kit.awaitTerminated(1, TimeUnit.MINUTES);
    }
    
    @Test
    public void sendCoinsToWalletAppKit() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        // Listen for the first coins-received transaction
        var transactionFuture = new CompletableFuture<Transaction>();
        kit.wallet().addCoinsReceivedEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            transactionFuture.complete(tx);
        });

        // Prepare the amount to send and destination address
        var amount = Coin.CENT;
        var receivingAddress = kit.wallet().currentReceiveAddress();

        // Make sure we have enough funds
        while (client.getBitcoinBalance(client.getRegTestMiningAddress()).value < amount.value) {
            client.generateBlocks(1);
        }

        // Send Bitcoin to the Receiving Address
        var txId = client.sendBitcoin(client.getRegTestMiningAddress(), receivingAddress, amount);
        // Mine a block to confirm the transaction
        client.generateBlocks(1);

        // Wait for WalletAppKit to notice the transaction
        var tx = transactionFuture.get(1, TimeUnit.MINUTES);

        // Verify correct amount received
        var balance = kit.wallet().getBalance();
        assertEquals(amount.value, balance.value);
    }
}
