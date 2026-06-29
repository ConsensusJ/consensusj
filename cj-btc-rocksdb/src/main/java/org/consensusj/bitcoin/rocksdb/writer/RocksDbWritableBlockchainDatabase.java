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
package org.consensusj.bitcoin.rocksdb.writer;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.rocksdb.AbstractRocksDbBlockchainDatabase;
import org.consensusj.bitcoin.rocksdb.schema.BlockchainSchema;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-write version of the blockchain database. Adds the put/delete operations on top of
 * {@link AbstractRocksDbBlockchainDatabase}. Writes are batched and only hit disk on {@link #flush()},
 * so remember to flush (close() will do it for you if you forget).
 */
public class RocksDbWritableBlockchainDatabase extends AbstractRocksDbBlockchainDatabase implements WritableBlockchainDatabase {
    private static final Logger log = LoggerFactory.getLogger(RocksDbWritableBlockchainDatabase.class);

    private final WriteBatch writeBatch;
    private final WriteOptions writeOptions;

    public RocksDbWritableBlockchainDatabase(Path dbPath, NetworkParameters networkParameters) throws RocksDBException {
        super(dbPath, networkParameters, false);
        this.writeBatch = new WriteBatch();
        this.writeOptions = new WriteOptions();
    }

    /**
     * Creates the database (with all the column families) and reopens it read-write.
     */
    public static RocksDbWritableBlockchainDatabase create(Path dbPath, NetworkParameters networkParameters) throws RocksDBException {
        RocksDB.loadLibrary();

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        for (String cfName : BlockchainSchema.ColumnFamilies.ALL) {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cfName.getBytes()));
        }

        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
        DBOptions options = new DBOptions()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        try (RocksDB db = RocksDB.open(options, dbPath.toString(), columnFamilyDescriptors, columnFamilyHandles)) {
            columnFamilyHandles.forEach(ColumnFamilyHandle::close);
            log.info("Created new blockchain database at {}", dbPath);
        } finally {
            options.close();
        }

        return new RocksDbWritableBlockchainDatabase(dbPath, networkParameters);
    }

    @Override
    public void putBlockHeader(int height, Block block) throws RocksDBException {
        // cloneAsHeader() drops the transactions so we serialize just the 80 byte header as the key
        byte[] key = block.cloneAsHeader().serialize();
        writeBatch.put(headersCF, key, BlockchainSchema.emptyValue());
        headersByHeight.put(height, block);
    }

    @Override
    public void updateChainTip(int height, Sha256Hash hash) throws RocksDBException {
        writeBatch.put(headersCF, BlockchainSchema.Keys.CHAIN_TIP, reverseBytes(hash.getBytes()));
    }

    @Override
    public void putTransactionHeight(Sha256Hash txid, int height) throws RocksDBException {
        writeBatch.put(txidCF, BlockchainSchema.txidKey(txid), BlockchainSchema.encodeHeight(height));
    }

    @Override
    public void putFunding(byte[] scriptHashPrefix, int height, byte[] txidPrefix, short vout) throws RocksDBException {
        if (scriptHashPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Script hash prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        if (txidPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Txid prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        byte[] key = BlockchainSchema.fundingKey(scriptHashPrefix, height);
        writeBatch.put(fundingCF, key, BlockchainSchema.emptyValue());
    }

    @Override
    public void putSpending(byte[] txidPrefix, short vout, int height, byte[] spendingTxidPrefix, int vin) throws RocksDBException {
        if (txidPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Txid prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        if (spendingTxidPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Spending txid prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        byte[] key = BlockchainSchema.spendingKey(txidPrefix, vout, height);
        writeBatch.put(spendingCF, key, BlockchainSchema.emptyValue());
    }

    @Override
    public void putConfig(String key, byte[] value) throws RocksDBException {
        writeBatch.put(configCF, key.getBytes(), value);
    }

    @Override
    public void deleteBlockHeader(int height) throws RocksDBException {
        // need the block to rebuild the header key
        Block block = headersByHeight.get(height);
        if (block != null) {
            writeBatch.delete(headersCF, block.cloneAsHeader().serialize());
            headersByHeight.remove(height);
        }
    }

    @Override
    public void deleteTransactionHeight(Sha256Hash txid) throws RocksDBException {
        writeBatch.delete(txidCF, BlockchainSchema.txidKey(txid));
    }

    @Override
    public void deleteFunding(byte[] scriptHashPrefix, int height, byte[] txidPrefix, short vout) throws RocksDBException {
        if (scriptHashPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Script hash prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        writeBatch.delete(fundingCF, BlockchainSchema.fundingKey(scriptHashPrefix, height));
    }

    @Override
    public void deleteSpending(byte[] txidPrefix, short vout, int height, byte[] spendingTxidPrefix, int vin) throws RocksDBException {
        if (txidPrefix.length != BlockchainSchema.PREFIX_LENGTH) {
            throw new IllegalArgumentException("Txid prefix must be " + BlockchainSchema.PREFIX_LENGTH + " bytes");
        }
        writeBatch.delete(spendingCF, BlockchainSchema.spendingKey(txidPrefix, vout, height));
    }

    @Override
    public void flush() throws RocksDBException {
        db.write(writeOptions, writeBatch);
        writeBatch.clear();
        log.debug("Flushed write batch to database");
    }

    @Override
    public void close() {
        try {
            // flush whatever is still pending so we don't silently lose writes
            if (writeBatch.count() > 0) {
                log.warn("Closing database with {} pending writes - flushing", writeBatch.count());
                flush();
            }
        } catch (RocksDBException e) {
            log.error("Error flushing pending writes on close", e);
        }

        log.info("Closing writable blockchain database");
        writeBatch.close();
        writeOptions.close();
        super.close();
    }
}
