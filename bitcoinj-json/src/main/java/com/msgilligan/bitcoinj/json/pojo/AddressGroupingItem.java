package com.msgilligan.bitcoinj.json.pojo;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;

/**
 * For listaddressgroupings response
 * Note: In the JSON response this is actually an array
 */
public class AddressGroupingItem {
    private final Address address;
    private final Coin balance;
    private final String account;

    public AddressGroupingItem(Address address, Coin balance, String account) {
        this.address = address;
        this.balance = balance;
        this.account = account;
    }

    public Address getAddress() {
        return address;
    }

    public Coin getBalance() {
        return balance;
    }

    public String getAccount() {
        return account;
    }
}
