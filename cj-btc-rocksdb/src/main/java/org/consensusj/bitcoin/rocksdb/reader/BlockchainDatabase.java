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
package org.consensusj.bitcoin.rocksdb.reader;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Block;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

/**
 * Read-only interface to a Bitcoin blockchain RocksDB database. Lets you query by script hash
 * (address history), transaction id and block height.
 *
 * @see org.consensusj.bitcoin.rocksdb.schema.BlockchainSchema
 */
public interface BlockchainDatabase extends Closeable {

    Optional<Integer> getChainTipHeight();

    Optional<Sha256Hash> getChainTipHash();

    Optional<Block> getBlockHeader(int height);

    Optional<Integer> getTransactionHeight(Sha256Hash txid);

    /** @param scriptHashPrefix first 8 bytes of SHA256(script) */
    List<Integer> getFundingHeights(byte[] scriptHashPrefix);

    List<Integer> getSpendingHeights(Sha256Hash txid, int vout);

    boolean isOpen();

    DatabaseStats getStats();

    record DatabaseStats(
        long fundingKeys,
        long spendingKeys,
        long txidKeys,
        long headerKeys,
        long totalSize
    ) {}
}
