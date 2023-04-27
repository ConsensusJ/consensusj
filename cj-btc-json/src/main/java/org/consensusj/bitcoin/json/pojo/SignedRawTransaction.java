package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.json.conversion.HexUtil;

/**
 *
 */
public class SignedRawTransaction {
    private final byte[] bytes;
    private final boolean complete;

    public static SignedRawTransaction of(Transaction transaction) {
        return new SignedRawTransaction(transaction.bitcoinSerialize(), true);
    }

    @JsonCreator
    public SignedRawTransaction(@JsonProperty("hex")        String   hex,
                                @JsonProperty("complete")   boolean  complete) {
        this(HexUtil.hexStringToByteArray(hex), complete);
    }

    private SignedRawTransaction(byte[] bytes, boolean complete) {
        this.bytes = bytes;
        this.complete = complete;
    }

    public  String getHex() {
        return HexUtil.bytesToHexString(bytes);
    }

    public boolean isComplete() {
        return complete;
    }
}
