package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.Coin;
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
        this.address = AddressParser.getDefault().parseAddress(address);
    }

    @Override
    public Coin amount() {
        return Coin.ofSat(amount);
    }

    @Override
    public Script scriptPubKey() {
        return ScriptBuilder.createOutputScript(address);
    }

    public Address address() {
        return address;
    }

}
