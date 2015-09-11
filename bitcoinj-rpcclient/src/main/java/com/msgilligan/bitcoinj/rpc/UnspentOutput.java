package com.msgilligan.bitcoinj.rpc;

import com.msgilligan.bitcoinj.rpc.conversion.BitcoinMath;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.math.BigDecimal;

/**
 * Data class for UnspentOutput as returned by listUnspent RPC
 */
public class UnspentOutput {
    private final Sha256Hash  txid;
    private final int         vout;
    private final Address     address;
    private final String      account;
    private final String      scriptPubKey;
    private final Coin        amount;
    private final int         confirmations;

    @Deprecated
    public UnspentOutput(Sha256Hash txid, int vout, Address address, String account, String scriptPubKey, BigDecimal amount, int confirmations) {
        this(txid, vout, address, account, scriptPubKey, BitcoinMath.btcToCoin(amount), confirmations);
    }

    public UnspentOutput(Sha256Hash txid, int vout, Address address, String account, String scriptPubKey, Coin amount, int confirmations) {
        this.txid = txid;
        this.vout = vout;
        this.address = address;
        this.account = account;
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        this.confirmations = confirmations;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getVout() {
        return vout;
    }

    public Address getAddress() {
        return address;
    }

    public String getAccount() {
        return account;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public Coin getAmount() {
        return amount;
    }

    public int getConfirmations() {
        return confirmations;
    }
}
