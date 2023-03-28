package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TODO: Merge/reconcile with WalletTransactionInfo
// TODO: Consider making more record-like
/**
 * (Mostly) Immutable representation of BitcoinTransaction info JSON
 * <p>
 * It is not fully-immutable because of {@link #addOtherInfo(String, Object)} which should only be used by Jackson
 * when deserializing.
 * <p>
 * This is returned by {@code BitcoinClient.listTransactions()}.
 */
public class BitcoinTransactionInfo {
    private final boolean           involvesWatchOnly;
    private final Address           address;
    private final String            category;
    private final Coin              amount;
    private final String            label;
    private final int               vout;
    private final Coin              fee;
    private final int               confirmations;
    private final boolean           generated;
    private final boolean           trusted;
    private final Sha256Hash        blockHash;
    private final int               blockHeight;
    private final int               blockIndex;
    private final Instant              blockTime;
    private final Sha256Hash            txId;
    private final List<Sha256Hash>      walletConflicts;
    private final Instant               time;
    private final Instant               timeReceived;
    private final String                comment;
    private final String                bip125Replaceable;
    private final boolean               abandoned;
    private final Map<String, Object>   otherInfo;

    public BitcoinTransactionInfo(@JsonProperty("involvesWatchonly")    boolean     involvesWatchOnly,
                                  @JsonProperty("address")              Address     address,
                                  @JsonProperty("category")             String      category,
                                  @JsonProperty("amount")               Coin        amount,
                                  @JsonProperty("label")                String      label,
                                  @JsonProperty("vout")                 int         vout,
                                  @JsonProperty("fee")                  Coin        fee,
                                  @JsonProperty("confirmations")        int         confirmations,
                                  @JsonProperty("generated")            boolean     generated,
                                  @JsonProperty("trusted")              boolean     trusted,
                                  @JsonProperty("blockhash")            Sha256Hash  blockHash,
                                  @JsonProperty("blockheight")          int         blockHeight,
                                  @JsonProperty("blockindex")           int         blockIndex,
                                  @JsonProperty("blockTime")            long        blockTime,
                                  @JsonProperty("txid")                 Sha256Hash  txId,
                                  @JsonProperty("walletconflicts")      List<Sha256Hash> walletConflicts,
                                  @JsonProperty("time")                 long        time,
                                  @JsonProperty("timereceived")         long        timeReceived,
                                  @JsonProperty("comment")              String      comment,
                                  @JsonProperty("bip125-replaceable")   String      bip125Replaceable,
                                  @JsonProperty("abandoned")            boolean     abandoned)
    {
        this.involvesWatchOnly = involvesWatchOnly;
        this.address = address;
        this.category = category;
        this.amount = amount;
        this.label = label;
        this.vout = vout;
        this.fee = fee;
        this.confirmations = confirmations;
        this.generated = generated;
        this.trusted = trusted;
        this.blockHash = blockHash;
        this.blockHeight = blockHeight;
        this.blockIndex = blockIndex;
        this.blockTime = Instant.ofEpochSecond(blockTime);
        this.txId = txId;
        this.walletConflicts = walletConflicts;
        this.time = Instant.ofEpochSecond(time);
        this.timeReceived = Instant.ofEpochSecond(timeReceived);
        this.comment = comment;
        this.bip125Replaceable = bip125Replaceable;
        this.abandoned = abandoned;
        this.otherInfo = new HashMap<>();
    }

    @JsonAnySetter
    public void addOtherInfo(String propertyKey, Object value) {
        this.otherInfo.put(propertyKey, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOtherInfo() {
        return otherInfo;
    }

    public boolean isInvolvesWatchOnly() {
        return involvesWatchOnly;
    }

    public Address getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public Coin getAmount() {
        return amount;
    }

    public String getLabel() {
        return (label != null) ? label.replace("\n", "") : null;
    }

    public int getVout() {
        return vout;
    }

    public Coin getFee() {
        return fee;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public Sha256Hash getBlockHash() {
        return blockHash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public Instant getBlockTime() {
        return blockTime;
    }

    public Sha256Hash getTxId() {
        return txId;
    }

    public List<Sha256Hash> getWalletConflicts() {
        return walletConflicts;
    }

    public Instant getTime() {
        return time;
    }

    public Instant getTimeReceived() {
        return timeReceived;
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public String isBip125Replaceable() {
        return bip125Replaceable;
    }

    public boolean isAbandoned() {
        return abandoned;
    }
}
