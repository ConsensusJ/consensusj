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
import org.consensusj.bitcoin.rocksdb.reader.BlockchainDatabase;

/**
 * Adds write operations to {@link BlockchainDatabase}. Everything is staged in a RocksDB WriteBatch
 * and only applied on {@link #flush()}, so a batch of puts/deletes lands atomically.
 *
 * The delete* methods exist mainly for reorg handling. All the *Prefix byte[] args are 8 bytes.
 */
public interface WritableBlockchainDatabase extends BlockchainDatabase {

    void putBlockHeader(int height, Block block) throws Exception;

    void updateChainTip(int height, Sha256Hash hash) throws Exception;

    void putTransactionHeight(Sha256Hash txid, int height) throws Exception;

    void putFunding(byte[] scriptHashPrefix, int height, byte[] txidPrefix, short vout) throws Exception;

    void putSpending(byte[] txidPrefix, short vout, int height, byte[] spendingTxidPrefix, int vin) throws Exception;

    void putConfig(String key, byte[] value) throws Exception;

    void deleteBlockHeader(int height) throws Exception;

    void deleteTransactionHeight(Sha256Hash txid) throws Exception;

    void deleteFunding(byte[] scriptHashPrefix, int height, byte[] txidPrefix, short vout) throws Exception;

    void deleteSpending(byte[] txidPrefix, short vout, int height, byte[] spendingTxidPrefix, int vin) throws Exception;

    /** Applies all batched writes to disk. */
    void flush() throws Exception;
}
