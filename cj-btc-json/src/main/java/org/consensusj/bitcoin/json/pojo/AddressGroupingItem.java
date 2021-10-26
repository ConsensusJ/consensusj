package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;

import java.util.List;

/**
 * For listaddressgroupings response
 * Note: In the JSON response this is actually an array
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressGroupingItem {
    private final Address address;
    private final Coin balance;
    private final String account;

    public AddressGroupingItem(Address address, Coin balance, String account) {
        this.address = address;
        this.balance = balance;
        this.account = account;
    }

    public AddressGroupingItem(List<Object> addressItem, NetworkParameters netParams) {
        String addressStr = (String) addressItem.get(0);
        //TODO: Try to avoid using Double
        Double balanceDouble = (Double) addressItem.get(1);
        account = (addressItem.size() > 2) ? (String) addressItem.get(2) : null;
        address = Address.fromString(netParams, addressStr);
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
