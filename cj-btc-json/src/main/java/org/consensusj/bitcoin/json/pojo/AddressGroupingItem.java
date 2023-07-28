package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.Coin;

import java.util.List;

/**
 * For {@code listaddressgroupings} response
 * Note: In the JSON response this is actually an array
 */
public class AddressGroupingItem {
    private static final AddressParser addressParser = AddressParser.getDefault();
    private final Address address;
    private final Coin balance;
    private final String account;

    public AddressGroupingItem(Address address, Coin balance, String account) {
        this.address = address;
        this.balance = balance;
        this.account = account;
    }

    @JsonCreator
    public AddressGroupingItem(List<Object> addressItem) {
        String addressStr = (String) addressItem.get(0);
        //TODO: Try to avoid using Double
        Double balanceDouble = (Double) addressItem.get(1);
        account = (addressItem.size() > 2) ? (String) addressItem.get(2) : null;
        address = addressParser.parseAddress(addressStr);
        balance = Coin.valueOf(((Double)(balanceDouble * 100000000.0)).longValue());

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
