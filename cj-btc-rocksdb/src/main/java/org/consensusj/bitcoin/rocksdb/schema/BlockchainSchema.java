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

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Column families and key formats for the blockchain RocksDB database.
 *
 * The layout is inspired by electrs - most indexes are key-only (empty value) to save space - but
 * the encoding is our own and is not byte-for-byte compatible with electrs.
 *
 * One gotcha: headers aren't keyed by height. The 80-byte serialized header is itself the key, so
 * to look one up by height you have to load every header and rebuild the chain from the tip.
 *
 * @see <a href="https://github.com/romanz/electrs/blob/master/doc/schema.md">Electrs Schema Documentation</a>
 */
public class BlockchainSchema {

    public static final class ColumnFamilies {
        public static final String FUNDING = "funding";    // script hash prefix -> height
        public static final String SPENDING = "spending";  // outpoint prefix -> spending height
        public static final String TXID = "txid";          // txid prefix -> confirmed height
        public static final String HEADERS = "headers";    // 80-byte header keys
        public static final String CONFIG = "config";
        public static final String DEFAULT = "default";    // required by RocksDB

        public static final String[] ALL = {
            DEFAULT, FUNDING, SPENDING, TXID, HEADERS, CONFIG
        };
    }

    public static final class Keys {
        public static final byte[] CHAIN_TIP = new byte[] { 'T' };
        public static final byte[] CONFIG = new byte[] { 'C' };
    }

    /** 8-byte prefixes are used for both script hashes and txids. */
    public static final int PREFIX_LENGTH = 8;

    public static final int HEADER_SIZE = 80;

    /** funding key = SHA256(script)[:8] + height, 12 bytes total. */
    public static byte[] fundingKey(byte[] scriptHashPrefix, int height) {
        if (scriptHashPrefix.length != PREFIX_LENGTH) {
            throw new IllegalArgumentException("Script hash prefix must be " + PREFIX_LENGTH + " bytes");
        }
        ByteBuffer buffer = ByteBuffer.allocate(PREFIX_LENGTH + 4);
        buffer.put(scriptHashPrefix);
        buffer.putInt(height);
        return buffer.array();
    }

    /** spending key = txid[:8] + vout + height, 14 bytes total. */
    public static byte[] spendingKey(byte[] txidPrefix, short vout, int height) {
        if (txidPrefix.length != PREFIX_LENGTH) {
            throw new IllegalArgumentException("Txid prefix must be " + PREFIX_LENGTH + " bytes");
        }
        ByteBuffer buffer = ByteBuffer.allocate(PREFIX_LENGTH + 2 + 4);
        buffer.put(txidPrefix);
        buffer.putShort(vout);
        buffer.putInt(height);
        return buffer.array();
    }

    public static byte[] txidKey(Sha256Hash txid) {
        return Arrays.copyOfRange(txid.getBytes(), 0, PREFIX_LENGTH);
    }

    public static byte[] txidKey(byte[] txidBytes) {
        return Arrays.copyOfRange(txidBytes, 0, PREFIX_LENGTH);
    }

    public static int decodeHeight(byte[] value) {
        if (value.length != 4) {
            throw new IllegalArgumentException("Height value must be 4 bytes, got " + value.length);
        }
        return ByteBuffer.wrap(value).getInt();
    }

    public static byte[] encodeHeight(int height) {
        return ByteBuffer.allocate(4).putInt(height).array();
    }

    public static byte[] extractScriptHashPrefix(byte[] fundingKey) {
        return Arrays.copyOfRange(fundingKey, 0, PREFIX_LENGTH);
    }

    public static int extractFundingHeight(byte[] fundingKey) {
        return ByteBuffer.wrap(fundingKey, PREFIX_LENGTH, 4).getInt();
    }

    public static byte[] extractTxidPrefix(byte[] spendingKey) {
        return Arrays.copyOfRange(spendingKey, 0, PREFIX_LENGTH);
    }

    public static short extractVout(byte[] spendingKey) {
        return ByteBuffer.wrap(spendingKey, PREFIX_LENGTH, 2).getShort();
    }

    public static int extractSpendingHeight(byte[] spendingKey) {
        return ByteBuffer.wrap(spendingKey, PREFIX_LENGTH + 2, 4).getInt();
    }

    /** Empty value used by the key-only indexes. */
    public static byte[] emptyValue() {
        return new byte[0];
    }
}
