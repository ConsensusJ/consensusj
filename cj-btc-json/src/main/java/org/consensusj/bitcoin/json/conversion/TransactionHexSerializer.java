package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Transaction;

import java.io.IOException;

/**
 * Serialize a transaction as a hex-encoded binary string.
 */
public class TransactionHexSerializer extends JsonSerializer<Transaction> {
    @Override
    public void serialize(Transaction value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(HexUtil.bytesToHexString(value.serialize()));
    }

}
