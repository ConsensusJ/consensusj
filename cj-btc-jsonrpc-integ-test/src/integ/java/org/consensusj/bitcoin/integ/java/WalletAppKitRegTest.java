package org.consensusj.bitcoin.integ.java;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.jsonrpc.RpcURI;
import org.consensusj.bitcoin.jsonrpc.test.TestServers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class WalletAppKitRegTest {
    static final NetworkParameters netParams = RegTestParams.get();
    static final private TestServers testServers = TestServers.getInstance();
    static final protected String rpcTestUser = testServers.getRpcTestUser();
    static final protected String rpcTestPassword = testServers.getRpcTestPassword();

    private static BitcoinExtendedClient client;
    WalletAppKit kit;

    @BeforeAll
    static void setupRegTestWallet() {
        client = new BitcoinExtendedClient(netParams, RpcURI.getDefaultRegTestWalletURI(), rpcTestUser, rpcTestPassword);
        client.initRegTestWallet();
    }

    @BeforeEach
    void setupTest(@TempDir File tempDir) throws UnknownHostException {
        kit = new WalletAppKit(netParams,
                ScriptType.P2WPKH,
                KeyChainGroupStructure.DEFAULT,
                tempDir,
                "prefix");
        kit.connectToLocalHost();
        kit.setBlockingStartup(false);
        // Start the wallet
        kit.startAsync();
        kit.awaitRunning();
    }

    @AfterEach
    void cleanupTest() throws TimeoutException {
        kit.stopAsync();
        kit.awaitTerminated(1, TimeUnit.MINUTES);
    }
    
    @Test
    public void sendCoinsToWalletAppKit() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        // Listen for the first coins-received transaction
        var transactionFuture = new CoinsReceivedFuture();
        kit.wallet().addCoinsReceivedEventListener(transactionFuture);

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
    
    static class CoinsReceivedFuture extends CompletableFuture<Transaction> implements WalletCoinsReceivedEventListener {
        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            this.complete(tx);
        }
    }
}
