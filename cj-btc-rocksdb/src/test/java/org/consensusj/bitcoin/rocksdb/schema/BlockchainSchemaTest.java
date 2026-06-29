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
package org.consensusj.bitcoin.rocksdb.schema;

import org.bitcoinj.base.Sha256Hash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the key encodings - byte lengths, round-tripping and the input validation.
 */
@DisplayName("BlockchainSchema Tests")
class BlockchainSchemaTest {

    @Test
    @DisplayName("Funding key should be 12 bytes (8-byte prefix + 4-byte height)")
    void testFundingKeyLength() {
        byte[] scriptHashPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        int height = 700000;

        byte[] fundingKey = BlockchainSchema.fundingKey(scriptHashPrefix, height);

        assertEquals(12, fundingKey.length, "Funding key must be exactly 12 bytes");
    }

    @Test
    @DisplayName("Funding key should contain script hash prefix and height")
    void testFundingKeyComponents() {
        byte[] scriptHashPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        int height = 700000;

        byte[] fundingKey = BlockchainSchema.fundingKey(scriptHashPrefix, height);

        byte[] extractedPrefix = BlockchainSchema.extractScriptHashPrefix(fundingKey);
        int extractedHeight = BlockchainSchema.extractFundingHeight(fundingKey);

        assertArrayEquals(scriptHashPrefix, extractedPrefix, "Script hash prefix should match");
        assertEquals(height, extractedHeight, "Height should match");
    }

    @Test
    @DisplayName("Spending key should be 14 bytes (8-byte txid + 2-byte vout + 4-byte height)")
    void testSpendingKeyLength() {
        byte[] txidPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        short vout = 0;
        int height = 700000;

        byte[] spendingKey = BlockchainSchema.spendingKey(txidPrefix, vout, height);

        assertEquals(14, spendingKey.length, "Spending key must be exactly 14 bytes");
    }

    @Test
    @DisplayName("Spending key should contain txid prefix, vout, and height")
    void testSpendingKeyComponents() {
        byte[] txidPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        short vout = 5;
        int height = 700000;

        byte[] spendingKey = BlockchainSchema.spendingKey(txidPrefix, vout, height);

        byte[] extractedTxidPrefix = BlockchainSchema.extractTxidPrefix(spendingKey);
        short extractedVout = BlockchainSchema.extractVout(spendingKey);
        int extractedHeight = BlockchainSchema.extractSpendingHeight(spendingKey);

        assertArrayEquals(txidPrefix, extractedTxidPrefix, "Txid prefix should match");
        assertEquals(vout, extractedVout, "Vout should match");
        assertEquals(height, extractedHeight, "Height should match");
    }

    @Test
    @DisplayName("Txid key should be 8 bytes (8-byte prefix)")
    void testTxidKeyLength() {
        Sha256Hash txid = Sha256Hash.wrap("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");

        byte[] txidKey = BlockchainSchema.txidKey(txid);

        assertEquals(8, txidKey.length, "Txid key must be exactly 8 bytes");
    }

    @Test
    @DisplayName("Empty value should be zero bytes (electrs key-only storage)")
    void testEmptyValue() {
        byte[] emptyValue = BlockchainSchema.emptyValue();

        assertNotNull(emptyValue, "Empty value should not be null");
        assertEquals(0, emptyValue.length, "Empty value must have zero length for electrs compatibility");
    }

    @Test
    @DisplayName("Height encoding should be 4 bytes")
    void testHeightEncoding() {
        int height = 700000;

        byte[] encoded = BlockchainSchema.encodeHeight(height);

        assertEquals(4, encoded.length, "Encoded height must be 4 bytes");

        int decoded = BlockchainSchema.decodeHeight(encoded);
        assertEquals(height, decoded, "Decoded height should match original");
    }

    @Test
    @DisplayName("Should reject invalid script hash prefix length")
    void testInvalidScriptHashPrefixLength() {
        byte[] invalidPrefix = new byte[]{1, 2, 3, 4};
        int height = 700000;

        assertThrows(IllegalArgumentException.class,
                () -> BlockchainSchema.fundingKey(invalidPrefix, height),
                "Should reject script hash prefix that is not 8 bytes");
    }

    @Test
    @DisplayName("Should reject invalid txid prefix length in spending key")
    void testInvalidTxidPrefixLengthInSpendingKey() {
        byte[] invalidPrefix = new byte[]{1, 2, 3, 4};
        short vout = 0;
        int height = 700000;

        assertThrows(IllegalArgumentException.class,
                () -> BlockchainSchema.spendingKey(invalidPrefix, vout, height),
                "Should reject txid prefix that is not 8 bytes");
    }

    @Test
    @DisplayName("Column families array should contain all required CFs")
    void testColumnFamiliesArray() {
        String[] cfs = BlockchainSchema.ColumnFamilies.ALL;

        assertEquals(6, cfs.length, "Should have exactly 6 column families");

        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.DEFAULT), "Should contain DEFAULT");
        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.FUNDING), "Should contain FUNDING");
        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.SPENDING), "Should contain SPENDING");
        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.TXID), "Should contain TXID");
        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.HEADERS), "Should contain HEADERS");
        assertTrue(contains(cfs, BlockchainSchema.ColumnFamilies.CONFIG), "Should contain CONFIG");
    }

    @Test
    @DisplayName("Prefix length should be 8 bytes")
    void testPrefixLength() {
        assertEquals(8, BlockchainSchema.PREFIX_LENGTH,
                "Prefix length must be 8 bytes for electrs compatibility");
    }

    @Test
    @DisplayName("Header size should be 80 bytes")
    void testHeaderSize() {
        assertEquals(80, BlockchainSchema.HEADER_SIZE,
                "Bitcoin block headers are always 80 bytes");
    }

    @Test
    @DisplayName("Special keys should be defined correctly")
    void testSpecialKeys() {
        assertEquals(1, BlockchainSchema.Keys.CHAIN_TIP.length,
                "Chain tip key should be 1 byte");
        assertEquals('T', BlockchainSchema.Keys.CHAIN_TIP[0],
                "Chain tip key should be 'T'");

        assertEquals(1, BlockchainSchema.Keys.CONFIG.length,
                "Config key should be 1 byte");
        assertEquals('C', BlockchainSchema.Keys.CONFIG[0],
                "Config key should be 'C'");
    }

    @Test
    @DisplayName("Height should support full range of int values")
    void testHeightRange() {
        int[] testHeights = {0, 1, 700000, Integer.MAX_VALUE};

        for (int height : testHeights) {
            byte[] encoded = BlockchainSchema.encodeHeight(height);
            int decoded = BlockchainSchema.decodeHeight(encoded);
            assertEquals(height, decoded, "Height " + height + " should round-trip correctly");
        }
    }

    @Test
    @DisplayName("Vout should support full range of short values")
    void testVoutRange() {
        byte[] txidPrefix = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        int height = 700000;

        short[] testVouts = {0, 1, 100, Short.MAX_VALUE};

        for (short vout : testVouts) {
            byte[] key = BlockchainSchema.spendingKey(txidPrefix, vout, height);
            short extractedVout = BlockchainSchema.extractVout(key);
            assertEquals(vout, extractedVout, "Vout " + vout + " should round-trip correctly");
        }
    }

    private boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
