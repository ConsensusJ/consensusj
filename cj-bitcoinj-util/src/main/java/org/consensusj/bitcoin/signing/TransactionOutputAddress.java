package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

/**
 * Raw, immutable data for a pay-to-address transaction output
 */
public class TransactionOutputAddress implements TransactionOutputData {
    private final long amount;
    private final Address address;

    public TransactionOutputAddress(Coin amount, Address address) {
        this.amount = amount.toSat();
        this.address = address;
    }

    public TransactionOutputAddress(long amount, Address address) {
        this.amount = amount;
        this.address = address;
    }

    public TransactionOutputAddress(long amount, String address) {
        this.amount = amount;
        this.address = Address.fromString(null, address);
    }

    @Override
    public String networkId() {
        return address.getParameters().getId();
    }

    @Override
    public Coin amount() {
        return Coin.ofSat(amount);
    }

    @Override
    public Script script() {
        return ScriptBuilder.createOutputScript(address);
    }

    public Address address() {
        return address;
    }

}
