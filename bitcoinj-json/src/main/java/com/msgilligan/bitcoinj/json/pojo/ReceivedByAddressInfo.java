package com.msgilligan.bitcoinj.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.util.List;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceivedByAddressInfo {
    public final Address address;
    public final String account;
    public final Coin amount;
    public final int confirmations;
    public final List<Sha256Hash> txids;
    public final String label;

    @JsonCreator
    public ReceivedByAddressInfo(@JsonProperty("address") Address address,
                                 @JsonProperty("account") String account,
                                 @JsonProperty("amount") Coin amount,
                                 @JsonProperty("confirmations") int confirmations,
                                 @JsonProperty("txids") List<Sha256Hash> txids,
                                 @JsonProperty("label") String label) {
        this.address = address;
        this.account = account;
        this.amount = amount;
        this.confirmations = confirmations;
        this.txids = txids;
        this.label = label;
    }

    public Address getAddress() {
        return address;
    }

    public String getAccount() {
        return account;
    }

    public Coin getAmount() {
        return amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public List<Sha256Hash> getTxids() {
        return txids;
    }
}
