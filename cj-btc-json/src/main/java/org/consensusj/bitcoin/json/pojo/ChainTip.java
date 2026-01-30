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
package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Sha256Hash;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class ChainTip {
    private final int height;
    private final Sha256Hash hash;
    private final int branchlen;
    private final String status;

    @JsonCreator
    public ChainTip(@JsonProperty("height")     int height,
                    @JsonProperty("hash")       Sha256Hash hash,
                    @JsonProperty("branchlen")  int branchlen,
                    @JsonProperty("status")     String status) {
        this.height = height;
        this.hash = hash;
        this.branchlen = branchlen;
        this.status = status;
    }

    public int getHeight() {
        return height;
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public int getBranchlen() {
        return branchlen;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Find the active chain tip if there is one
     * @param chainTips the list to search
     * @return non-empty optional if active is found, empty optional if not found
     */
    public static Optional<ChainTip> findActiveChainTip(List<ChainTip> chainTips) {
        return chainTips.stream().filter(tip -> tip.getStatus().equals("active")).findFirst();
    }

    public static ChainTip findActiveChainTipOrElseThrow(List<ChainTip> chainTips) {
        return findActiveChainTip(chainTips).orElseThrow(() -> new RuntimeException("No active ChainTip"));
    }

    /**
     * Construct an "active" {@link ChainTip}
     *
     * @param height best block height
     * @param hash best block hash
     * @return current "active" ChainTip
     */
    public static ChainTip ofActive(int height, Sha256Hash hash) {
        return new ChainTip(height, hash, 0, "active");
    }
}
