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
package org.consensusj.bitcoin.rocksdb.integration;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.rocksdb.reader.BlockchainDatabase;
import org.consensusj.bitcoin.rocksdb.reader.RocksDbBlockchainDatabase;
import org.consensusj.bitcoin.rocksdb.schema.BlockchainSchema;
import org.consensusj.bitcoin.rocksdb.writer.RocksDbWritableBlockchainDatabase;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trips data through the writer and back out the reader against a real temp RocksDB, to make
 * sure the two stay compatible.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Blockchain Database Integration Tests")
class BlockchainDatabaseIntegrationTest {

    private Path tempDbPath;
    private NetworkParameters params;
    private Block genesisBlock;
    private Block block1;
    private Block block2;

    @BeforeAll
    void setup() throws Exception {
        tempDbPath = Files.createTempDirectory("test-blockchain-db-");
        params = RegTestParams.get();

        genesisBlock = params.getGenesisBlock();
        block1 = createNextBlock(genesisBlock, 1);
        block2 = createNextBlock(block1, 2);
    }

    @AfterAll
    void cleanup() throws IOException {
        if (tempDbPath != null && Files.exists(tempDbPath)) {
            Files.walk(tempDbPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path);
                        }
                    });
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should create database and write genesis block")
    void testWriteGenesisBlock() throws Exception {
        try (var writer = RocksDbWritableBlockchainDatabase.create(tempDbPath, params)) {
            assertTrue(writer.isOpen(), "Database should be open after creation");
            writer.putBlockHeader(0, genesisBlock);
            writer.updateChainTip(0, genesisBlock.getHash());
            writer.flush();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should read back genesis block")
    void testReadGenesisBlock() throws Exception {
        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            assertTrue(reader.isOpen(), "Database should be open");

            Optional<Integer> tipHeight = reader.getChainTipHeight();
            assertTrue(tipHeight.isPresent(), "Chain tip height should be present");
            assertEquals(0, tipHeight.get(), "Chain tip should be at height 0");

            Optional<Sha256Hash> tipHash = reader.getChainTipHash();
            assertTrue(tipHash.isPresent(), "Chain tip hash should be present");
            assertEquals(genesisBlock.getHash(), tipHash.get(), "Chain tip hash should match genesis");

            Optional<Block> readGenesis = reader.getBlockHeader(0);
            assertTrue(readGenesis.isPresent(), "Genesis block should be readable");
            assertEquals(genesisBlock.getHash(), readGenesis.get().getHash(), "Genesis block hash should match");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should write multiple blocks and build chain")
    void testWriteMultipleBlocks() throws Exception {
        try (var writer = new RocksDbWritableBlockchainDatabase(tempDbPath, params)) {
            writer.putBlockHeader(1, block1);
            writer.putBlockHeader(2, block2);
            writer.updateChainTip(2, block2.getHash());
            writer.flush();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should read back entire chain and verify consistency")
    void testReadMultipleBlocks() throws Exception {
        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            Optional<Integer> tipHeight = reader.getChainTipHeight();
            assertTrue(tipHeight.isPresent(), "Chain tip height should be present");
            assertEquals(2, tipHeight.get(), "Chain tip should be at height 2");

            Optional<Sha256Hash> tipHash = reader.getChainTipHash();
            assertTrue(tipHash.isPresent(), "Chain tip hash should be present");
            assertEquals(block2.getHash(), tipHash.get(), "Chain tip should be block 2");

            Optional<Block> readGenesis = reader.getBlockHeader(0);
            Optional<Block> readBlock1 = reader.getBlockHeader(1);
            Optional<Block> readBlock2 = reader.getBlockHeader(2);

            assertTrue(readGenesis.isPresent(), "Genesis should be readable");
            assertTrue(readBlock1.isPresent(), "Block 1 should be readable");
            assertTrue(readBlock2.isPresent(), "Block 2 should be readable");

            assertEquals(genesisBlock.getHash(), readGenesis.get().getHash(), "Genesis hash should match");
            assertEquals(block1.getHash(), readBlock1.get().getHash(), "Block 1 hash should match");
            assertEquals(block2.getHash(), readBlock2.get().getHash(), "Block 2 hash should match");

            // the prevBlockHash links are what loadHeaders() walks, so check they survived the round trip
            assertEquals(genesisBlock.getHash(), readBlock1.get().getPrevBlockHash(), "Block 1 should link to genesis");
            assertEquals(block1.getHash(), readBlock2.get().getPrevBlockHash(), "Block 2 should link to block 1");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should write and read transaction heights")
    void testTransactionIndexing() throws Exception {
        // the index keys on the first 8 bytes, so these txids have to differ in there leading bytes
        Sha256Hash tx1 = Sha256Hash.wrap("1100000000000000000000000000000000000000000000000000000000000000");
        Sha256Hash tx2 = Sha256Hash.wrap("2200000000000000000000000000000000000000000000000000000000000000");
        Sha256Hash tx3 = Sha256Hash.wrap("3300000000000000000000000000000000000000000000000000000000000000");

        try (var writer = new RocksDbWritableBlockchainDatabase(tempDbPath, params)) {
            writer.putTransactionHeight(tx1, 0);
            writer.putTransactionHeight(tx2, 1);
            writer.putTransactionHeight(tx3, 2);
            writer.flush();
        }

        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            Optional<Integer> height1 = reader.getTransactionHeight(tx1);
            Optional<Integer> height2 = reader.getTransactionHeight(tx2);
            Optional<Integer> height3 = reader.getTransactionHeight(tx3);

            assertTrue(height1.isPresent(), "Transaction 1 height should be present");
            assertTrue(height2.isPresent(), "Transaction 2 height should be present");
            assertTrue(height3.isPresent(), "Transaction 3 height should be present");

            assertEquals(0, height1.get(), "Transaction 1 should be in genesis block");
            assertEquals(1, height2.get(), "Transaction 2 should be in block 1");
            assertEquals(2, height3.get(), "Transaction 3 should be in block 2");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should write and read funding indices (key-only storage)")
    void testFundingIndex() throws Exception {
        byte[] scriptHashPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        byte[] txidPrefix = new byte[]{10, 11, 12, 13, 14, 15, 16, 17};
        short vout = 0;

        try (var writer = new RocksDbWritableBlockchainDatabase(tempDbPath, params)) {
            writer.putFunding(scriptHashPrefix, 0, txidPrefix, vout);
            writer.putFunding(scriptHashPrefix, 1, txidPrefix, (short) 1);
            writer.putFunding(scriptHashPrefix, 2, txidPrefix, (short) 2);
            writer.flush();
        }

        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            List<Integer> heights = reader.getFundingHeights(scriptHashPrefix);

            assertNotNull(heights, "Funding heights list should not be null");
            assertEquals(3, heights.size(), "Should have 3 funding entries");
            assertTrue(heights.contains(0), "Should have funding at height 0");
            assertTrue(heights.contains(1), "Should have funding at height 1");
            assertTrue(heights.contains(2), "Should have funding at height 2");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should write and read spending indices (key-only storage)")
    void testSpendingIndex() throws Exception {
        // reader derives the lookup prefix from fundingTxid, so derive the written prefix the same way
        Sha256Hash fundingTxid = Sha256Hash.wrap("aa00000000000000000000000000000000000000000000000000000000000000");
        byte[] fundingTxidPrefix = BlockchainSchema.txidKey(fundingTxid);
        byte[] spendingTxidPrefix = new byte[]{0, 0, 0, 0, 0, 0, 0, 2};
        short vout = 0;
        int vin = 0;

        try (var writer = new RocksDbWritableBlockchainDatabase(tempDbPath, params)) {
            writer.putSpending(fundingTxidPrefix, vout, 1, spendingTxidPrefix, vin);
            writer.flush();
        }

        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            List<Integer> heights = reader.getSpendingHeights(fundingTxid, vout);

            assertNotNull(heights, "Spending heights list should not be null");
            assertEquals(1, heights.size(), "Should have 1 spending entry");
            assertEquals(1, heights.get(0), "Spending should be at height 1");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should get database statistics")
    void testDatabaseStats() throws Exception {
        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            BlockchainDatabase.DatabaseStats stats = reader.getStats();

            assertNotNull(stats, "Stats should not be null");
            assertTrue(stats.fundingKeys() >= 3, "Should have at least 3 funding keys");
            assertTrue(stats.spendingKeys() >= 1, "Should have at least 1 spending key");
            assertTrue(stats.txidKeys() >= 3, "Should have at least 3 txid keys");
            assertTrue(stats.headerKeys() >= 3, "Should have at least 3 header keys");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Should handle non-existent data gracefully")
    void testNonExistentData() throws Exception {
        try (var reader = new RocksDbBlockchainDatabase(tempDbPath, params)) {
            Optional<Block> nonExistent = reader.getBlockHeader(100);
            assertFalse(nonExistent.isPresent(), "Non-existent block should return empty");

            Sha256Hash nonExistentTx = Sha256Hash.wrap("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
            Optional<Integer> txHeight = reader.getTransactionHeight(nonExistentTx);
            assertFalse(txHeight.isPresent(), "Non-existent transaction should return empty");

            byte[] nonExistentScript = new byte[]{99, 99, 99, 99, 99, 99, 99, 99};
            List<Integer> fundingHeights = reader.getFundingHeights(nonExistentScript);
            assertTrue(fundingHeights.isEmpty(), "Non-existent script should return empty list");
        }
    }

    // a bare header on top of prevBlock - merkle root is zeroed since we don't care about txs here
    private Block createNextBlock(Block prevBlock, long timeSeconds) {
        return new Block(
                2L,
                prevBlock.getHash(),
                Sha256Hash.ZERO_HASH,
                timeSeconds,
                prevBlock.getDifficultyTarget(),
                0L,
                java.util.Collections.emptyList()
        );
    }
}
