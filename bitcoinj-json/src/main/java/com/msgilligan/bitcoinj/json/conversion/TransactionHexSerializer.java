package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Transaction;

import java.io.IOException;
import java.util.Formatter;

/**
 *
 */
public class TransactionHexSerializer extends JsonSerializer<Transaction> {
    @Override
    public void serialize(Transaction value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(bytesToHexString(value.bitcoinSerialize()));
    }

    // From: http://bitcoin.stackexchange.com/questions/8475/how-to-get-hex-string-from-transaction-in-bitcoinj
    public static String bytesToHexString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        formatter.close();
        return sb.toString();
    }
}
