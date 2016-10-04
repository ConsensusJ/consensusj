package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigDecimal;

/**
 * POJO for `getblockchaininfo` RPC response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockChainInfo {
    private final String      chain;
    private final int         blocks;
    private final int         headers;
    private final Sha256Hash  bestBlockHash;
    private final BigDecimal  difficulty;
    private final BigDecimal  verificationProgress;
    private final byte[]      chainWork;

    @JsonCreator
    public BlockChainInfo(@JsonProperty("chain")            String chain,
                          @JsonProperty("blocks")           int blocks,
                          @JsonProperty("headers")          int headers,
                          @JsonProperty("bestblockhash")    Sha256Hash bestBlockHash,
                          @JsonProperty("chain")            BigDecimal difficulty,
                          @JsonProperty("chain")            BigDecimal verificationProgress,
                          @JsonProperty("chain")            byte[] chainWork) {
        this.chain = chain;
        this.blocks = blocks;
        this.headers = headers;
        this.bestBlockHash = bestBlockHash;
        this.difficulty = difficulty;
        this.verificationProgress = verificationProgress;
        this.chainWork = chainWork;
    }

    public String getChain() {
        return chain;
    }

    public int getBlocks() {
        return blocks;
    }

    public int getHeaders() {
        return headers;
    }

    public Sha256Hash getBestBlockHash() {
        return bestBlockHash;
    }

    public BigDecimal getDifficulty() {
        return difficulty;
    }

    public BigDecimal getVerificationProgress() {
        return verificationProgress;
    }

    public byte[] getChainWork() {
        return chainWork;
    }
}
