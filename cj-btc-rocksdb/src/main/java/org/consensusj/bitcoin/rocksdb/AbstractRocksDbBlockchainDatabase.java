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
package org.consensusj.bitcoin.rocksdb;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.consensusj.bitcoin.rocksdb.reader.BlockchainDatabase;
import org.consensusj.bitcoin.rocksdb.schema.BlockchainSchema;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared read implementation for {@link BlockchainDatabase}.
 *
 * Opens the RocksDB database, maps the column families from {@link BlockchainSchema} and builds
 * an in-memory height index by walking the header chain back from the tip. Subclasses just pick
 * the open mode (read-only reader vs. read-write writer).
 */
public abstract class AbstractRocksDbBlockchainDatabase implements BlockchainDatabase {
    private static final Logger log = LoggerFactory.getLogger(AbstractRocksDbBlockchainDatabase.class);

    protected final RocksDB db;
    protected final List<ColumnFamilyHandle> columnFamilyHandles;
    protected final ColumnFamilyHandle fundingCF;
    protected final ColumnFamilyHandle spendingCF;
    protected final ColumnFamilyHandle txidCF;
    protected final ColumnFamilyHandle headersCF;
    protected final ColumnFamilyHandle configCF;
    protected final NetworkParameters networkParameters;

    // headers are not keyed by height on disk, so we keep this index in memory
    protected final Map<Integer, Block> headersByHeight = new HashMap<>();

    private final DBOptions options;

    protected AbstractRocksDbBlockchainDatabase(Path dbPath, NetworkParameters networkParameters, boolean readOnly) throws RocksDBException {
        if (networkParameters == null) {
            throw new IllegalArgumentException("networkParameters cannot be null");
        }
        this.networkParameters = networkParameters;
        this.columnFamilyHandles = new ArrayList<>();

        RocksDB.loadLibrary();

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        for (String cfName : BlockchainSchema.ColumnFamilies.ALL) {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cfName.getBytes()));
        }

        this.options = new DBOptions()
                .setCreateIfMissing(false)
                .setCreateMissingColumnFamilies(false);

        try {
            this.db = readOnly
                    ? RocksDB.openReadOnly(options, dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles)
                    : RocksDB.open(options, dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles);

            this.fundingCF = findColumnFamily(BlockchainSchema.ColumnFamilies.FUNDING);
            this.spendingCF = findColumnFamily(BlockchainSchema.ColumnFamilies.SPENDING);
            this.txidCF = findColumnFamily(BlockchainSchema.ColumnFamilies.TXID);
            this.headersCF = findColumnFamily(BlockchainSchema.ColumnFamilies.HEADERS);
            this.configCF = findColumnFamily(BlockchainSchema.ColumnFamilies.CONFIG);

            log.info("Opened blockchain database at {} ({}) with {} column families",
                    dbPath, readOnly ? "read-only" : "read-write", columnFamilyHandles.size());

            loadHeaders();
            log.info("Loaded {} block headers into memory", headersByHeight.size());
        } catch (RocksDBException e) {
            // don't leak the native handles if the open blew up half way through
            columnFamilyHandles.forEach(ColumnFamilyHandle::close);
            options.close();
            throw e;
        }
    }

    private ColumnFamilyHandle findColumnFamily(String name) {
        for (int i = 0; i < BlockchainSchema.ColumnFamilies.ALL.length; i++) {
            if (BlockchainSchema.ColumnFamilies.ALL[i].equals(name)) {
                return columnFamilyHandles.get(i);
            }
        }
        throw new IllegalStateException("Column family not found: " + name);
    }

    private void loadHeaders() throws RocksDBException {
        Map<Sha256Hash, Block> headerMap = new HashMap<>();

        try (RocksIterator iterator = db.newIterator(headersCF)) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                byte[] key = iterator.key();

                if (key.length == 1 && key[0] == 'T') {        // the chain-tip marker, not a header
                    iterator.next();
                    continue;
                }

                if (key.length == BlockchainSchema.HEADER_SIZE) {
                    try {
                        Block block = networkParameters.getDefaultSerializer().makeBlock(ByteBuffer.wrap(key));
                        headerMap.put(block.getHash(), block);
                    } catch (ProtocolException e) {
                        log.warn("Failed to deserialize header, skipping: {}", e.getMessage());
                    }
                } else {
                    log.warn("Skipping header-CF key with unexpected length: {} bytes", key.length);
                }
                iterator.next();
            }
        }

        Optional<Sha256Hash> tipHashOpt = getChainTipHash();
        if (tipHashOpt.isEmpty()) {
            log.warn("No chain tip found, cannot build height index");
            return;
        }

        // walk back from the tip following prevBlockHash, so the list comes out tip-first
        List<Sha256Hash> chain = new ArrayList<>();
        Sha256Hash currentHash = tipHashOpt.get();
        while (headerMap.containsKey(currentHash)) {
            Block header = headerMap.get(currentHash);
            chain.add(currentHash);
            currentHash = header.getPrevBlockHash();
        }

        int tipHeight = chain.size() - 1;
        for (int i = 0; i < chain.size(); i++) {
            headersByHeight.put(tipHeight - i, headerMap.get(chain.get(i)));
        }

        log.debug("Built chain with {} headers, tip at height {}", headersByHeight.size(), tipHeight);
    }

    @Override
    public Optional<Integer> getChainTipHeight() {
        if (headersByHeight.isEmpty()) {
            return Optional.empty();
        }
        return headersByHeight.keySet().stream().max(Integer::compareTo);
    }

    @Override
    public Optional<Sha256Hash> getChainTipHash() {
        try {
            byte[] tipHash = db.get(headersCF, BlockchainSchema.Keys.CHAIN_TIP);
            if (tipHash == null) {
                return Optional.empty();
            }
            // stored in internal byte oder, Sha256Hash wants it reversed
            return Optional.of(Sha256Hash.wrap(reverseBytes(tipHash)));
        } catch (RocksDBException e) {
            log.error("Error getting chain tip hash", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Block> getBlockHeader(int height) {
        return Optional.ofNullable(headersByHeight.get(height));
    }

    @Override
    public Optional<Integer> getTransactionHeight(Sha256Hash txid) {
        try {
            byte[] heightBytes = db.get(txidCF, BlockchainSchema.txidKey(txid));
            if (heightBytes == null) {
                return Optional.empty();
            }
            return Optional.of(BlockchainSchema.decodeHeight(heightBytes));
        } catch (RocksDBException e) {
            log.error("Error getting transaction height for {}", txid, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Integer> getFundingHeights(byte[] scriptHashPrefix) {
        if (scriptHashPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Script hash prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }

        List<Integer> heights = new ArrayList<>();
        try (RocksIterator iterator = db.newIterator(fundingCF)) {
            iterator.seek(scriptHashPrefix);
            while (iterator.isValid()) {
                byte[] key = iterator.key();
                if (!startsWithPrefix(key, scriptHashPrefix)) {
                    break;
                }
                heights.add(BlockchainSchema.extractFundingHeight(key));
                iterator.next();
            }
        }
        return heights;
    }

    @Override
    public List<Integer> getSpendingHeights(Sha256Hash txid, int vout) {
        byte[] txidPrefix = BlockchainSchema.txidKey(txid);
        List<Integer> heights = new ArrayList<>();

        try (RocksIterator iterator = db.newIterator(spendingCF)) {
            // seek to the first key for this txid+vout, then read until the prefix stops matching
            byte[] prefixKey = BlockchainSchema.spendingKey(txidPrefix, (short) vout, 0);
            iterator.seek(Arrays.copyOfRange(prefixKey, 0, BlockchainSchema.PREFIX_LENGTH + 2));

            while (iterator.isValid()) {
                byte[] key = iterator.key();
                byte[] keyTxidPrefix = BlockchainSchema.extractTxidPrefix(key);
                short keyVout = BlockchainSchema.extractVout(key);
                if (!Arrays.equals(keyTxidPrefix, txidPrefix) || keyVout != vout) {
                    break;
                }
                heights.add(BlockchainSchema.extractSpendingHeight(key));
                iterator.next();
            }
        }
        return heights;
    }

    @Override
    public boolean isOpen() {
        return db != null && db.isOwningHandle();
    }

    @Override
    public DatabaseStats getStats() {
        long fundingKeys = estimateNumKeys(fundingCF);
        long spendingKeys = estimateNumKeys(spendingCF);
        long txidKeys = estimateNumKeys(txidCF);
        long headerKeys = estimateNumKeys(headersCF);
        long totalSize = 0;

        try {
            totalSize = Long.parseLong(db.getProperty("rocksdb.total-sst-files-size"));
        } catch (RocksDBException e) {
            log.warn("Could not get database size", e);
        }

        return new DatabaseStats(fundingKeys, spendingKeys, txidKeys, headerKeys, totalSize);
    }

    @Override
    public void close() {
        log.info("Closing blockchain database");
        columnFamilyHandles.forEach(ColumnFamilyHandle::close);
        db.close();
        options.close();
    }

    private long estimateNumKeys(ColumnFamilyHandle cf) {
        try {
            return Long.parseLong(db.getProperty(cf, "rocksdb.estimate-num-keys"));
        } catch (RocksDBException e) {
            log.warn("Could not estimate number of keys", e);
            return 0;
        }
    }

    private static boolean startsWithPrefix(byte[] key, byte[] prefix) {
        if (key.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (key[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    // reversed copy, for converting between the db's internal hash order and bitcoinj's display order
    protected static byte[] reverseBytes(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - 1 - i];
        }
        return reversed;
    }
}
