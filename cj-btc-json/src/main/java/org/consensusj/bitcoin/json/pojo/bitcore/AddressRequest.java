package org.consensusj.bitcoin.json.pojo.bitcore;

import org.bitcoinj.core.Address;

import java.util.List;

/**
 * Address Request for BitCore {@code getaddressbalance} and others.
 * Typically passed in the request as a JSON-RPC parameter.
 */
public class AddressRequest {
    private final List<Address> addresses;
    public AddressRequest(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
