package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.Sha256Hash;

import java.math.BigDecimal;

/**
 * POJO for `getblockchaininfo` RPC response.
 */
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
                          @JsonProperty("bestblockhash")        Sha256Hash bestBlockHash,
                          @JsonProperty("difficulty")           BigDecimal difficulty,
                          @JsonProperty("verificationprogress") BigDecimal verificationProgress,
                          @JsonProperty("chainwork")            byte[] chainWork) {
        this.chain = chain;
        this.blocks = blocks;
        this.headers = headers;
        this.bestBlockHash = bestBlockHash;
        this.difficulty = difficulty;
        this.verificationProgress = verificationProgress;
        this.chainWork = chainWork;
    }

    /**
     *
     * @return a short string identifying which chain (Note: this differs from {@link BitcoinNetwork#toString()}
     */
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

    /**
     * Map a BlockChainInfo chain string to a Network. These strings are different from the standard values
     * in {@link BitcoinNetwork#toString()}.
     * @param info {@code BlockChainInfo}
     * @return the matching network.
     */
    public static Network chainToNetwork(BlockChainInfo info) {
        Network network;
        switch(info.getChain()) {
            case "main":
                network = BitcoinNetwork.MAINNET;
                break;
            case "test":
                network = BitcoinNetwork.TESTNET;
                break;
            case "signet":
                network = BitcoinNetwork.SIGNET;
                break;
            case "regtest":
                network = BitcoinNetwork.REGTEST;
                break;
            default:
                throw new RuntimeException("BlockChainInfo contains unrecognized Bitcoin network");
        }
        return network;
    }

    /**
     * Map {@link BitcoinNetwork} to a chain-id string.
     * Bitcoin Core returns strings that differ from {@link BitcoinNetwork#toString()}.
     * @param network bitcoinj enum type
     * @return Bitcoin Core-compatible <q>chain</q> string
     */
    public static String networkToChainName(BitcoinNetwork network) {
        String name;
        switch(network) {
            case MAINNET:
                name = "main";
                break;
            case TESTNET:
                name = "test";
                break;
            default:
                name = network.toString();
                break;
        };
        return name;
    }
}
