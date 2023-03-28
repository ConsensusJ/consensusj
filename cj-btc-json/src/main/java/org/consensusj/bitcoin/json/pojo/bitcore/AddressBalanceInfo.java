package org.consensusj.bitcoin.json.pojo.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Coin;

/**
 * Result of OmniCore/BitCore {@code getaddressbalance} method.
 */
public class AddressBalanceInfo {
    private final long balance;
    private final long received;
    private final long immature;

    public AddressBalanceInfo(@JsonProperty("balance") long balance,
                              @JsonProperty("received") long received,
                              @JsonProperty("immature") long immature) {
        this.balance = balance;
        this.received = received;
        this.immature = immature;
    }

    /**
     * @return the current balance in satoshis
     */
    public Coin getBalance() {
        return Coin.ofSat(balance);
    }

    /**
     * @return the total number of satoshis received (including change)
     */
    public Coin getReceived() {
        return Coin.ofSat(received);
    }

    /**
     * @return the total number of non-spendable mining satoshis received
     */
    public Coin getImmature() {
        return Coin.ofSat(immature);
    }
}
