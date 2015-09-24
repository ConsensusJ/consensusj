package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Transaction;

import java.io.IOException;

/**
 * Custom Serialization of Transaction Peer class
 */
public class TransactionSerializer extends JsonSerializer<Transaction> {
    @Override
    public void serialize(Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("hash", value.getHashAsString());
        jgen.writeEndObject();
    }
}
