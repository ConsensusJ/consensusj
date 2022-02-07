package org.consensusj.bitcoin.json.pojo.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Sha256Hash;

import java.util.List;

/**
 * Result from Bitcore/Omni {@code getaddressutxos}.
 */
public class AddressUtxoResult {
    private final Sha256Hash hash;
    private final int height;
    private final List<AddressUtxoInfo> utxos;

    public AddressUtxoResult(@JsonProperty("hash")    Sha256Hash hash,
                             @JsonProperty("height")  int height,
                             @JsonProperty("utxos")   List<AddressUtxoInfo> utxos) {
        this.hash = hash;
        this.height = height;
        this.utxos = utxos;
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public int getHeight() {
        return height;
    }

    public List<AddressUtxoInfo> getUtxos() {
        return utxos;
    }
}
