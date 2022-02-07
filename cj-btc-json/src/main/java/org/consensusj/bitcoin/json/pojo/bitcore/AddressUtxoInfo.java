package org.consensusj.bitcoin.json.pojo.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

/**
 * UTXO Info object from Bitcore/Omni {@code getaddressutxos}.
 */
public class AddressUtxoInfo {
    private final Address address;
    private final Sha256Hash txid;
    private final int outputIndex;
    private final byte[] script;
    private final int satoshis;
    private final int height;
    private final boolean coinbase;

    public AddressUtxoInfo(@JsonProperty("address")     Address address,
                           @JsonProperty("txid")        Sha256Hash txid,
                           @JsonProperty("outputIndex") int outputIndex,
                           @JsonProperty("script")      byte[] script,
                           @JsonProperty("satoshis")    int satoshis,
                           @JsonProperty("height")      int height,
                           @JsonProperty("coinbase")    boolean coinbase) {
        this.address = address;
        this.txid = txid;
        this.outputIndex = outputIndex;
        this.script = script;
        this.satoshis = satoshis;
        this.height = height;
        this.coinbase = coinbase;
    }

    public Address getAddress() {
        return address;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public byte[] getScript() {
        return script;
    }

    public int getSatoshis() {
        return satoshis;
    }

    public int getHeight() {
        return height;
    }

    public boolean isCoinbase() {
        return coinbase;
    }
}
